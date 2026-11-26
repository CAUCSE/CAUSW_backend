package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaignRecipient;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRecipientRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailCampaignDispatchStore {

	private final EmailCampaignRepository campaignRepository;
	private final EmailCampaignRecipientRepository recipientRepository;

	/**
	 * 새 발송 요청을 SENDING으로 시작하거나 이미 진행 중인 캠페인의 재개 가능 여부를 반환한다.
	 * <p>호출자의 트랜잭션과 분리된 새 트랜잭션에서 상태를 저장한다.</p>
	 * @param campaignId 준비할 캠페인 ID
	 * @param now 발송 시작 시각
	 * @return 발송 작업을 계속할 수 있으면 true
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean prepare(String campaignId, LocalDateTime now) {
		EmailCampaign campaign = getCampaign(campaignId);
		if (campaign.getStatus() == EmailCampaignStatus.QUEUED) {
			campaign.start(now);
			return true;
		}
		return campaign.getStatus() == EmailCampaignStatus.SENDING;
	}

	/**
	 * 현재 발송 가능한 수신자를 조건부 claim하고 SES 호출에 필요한 불변 데이터를 반환한다.
	 * @param campaignId 캠페인 ID
	 * @param batchSize 한 번에 claim할 최대 수신자 수
	 * @param now claim 및 재시도 가능 여부 기준 시각
	 * @return claim에 성공한 발송 대상 목록
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<EmailCampaignDispatchTarget> claimBatch(String campaignId, int batchSize, LocalDateTime now) {
		EmailCampaign campaign = getCampaign(campaignId);
		List<Long> ids = recipientRepository.findDispatchableIds(
			campaignId, EmailCampaignRecipientStatus.PENDING, now, PageRequest.of(0, batchSize));
		List<EmailCampaignDispatchTarget> targets = new ArrayList<>();
		for (Long id : ids) {
			int claimed = recipientRepository.claim(
				id, EmailCampaignRecipientStatus.PENDING, EmailCampaignRecipientStatus.SENDING, now);
			if (claimed == 1) {
				EmailCampaignRecipient recipient = getRecipient(id);
				targets.add(new EmailCampaignDispatchTarget(
					recipient.getId(), campaign.getFromAddress(), campaign.getReplyToAddress(),
					recipient.getEmailSnapshot(), campaign.getSubject(), campaign.getSanitizedHtml(),
					recipient.getAttemptCount()));
			}
		}
		return targets;
	}

	/**
	 * SES 호출 결과를 성공, 재시도 대기 또는 영구 실패 상태로 저장한다.
	 * @param outcome 수신자별 발송 결과
	 * @param maxAttempts 최대 발송 시도 횟수
	 * @param initialBackoffSeconds 첫 재시도 대기 시간
	 * @param now 결과 처리 시각
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordOutcome(
		EmailCampaignSendOutcome outcome, int maxAttempts, long initialBackoffSeconds, LocalDateTime now) {
		EmailCampaignRecipient recipient = getRecipient(outcome.recipientId());
		if (outcome.isSuccess()) {
			recipient.markSent(outcome.messageId(), now);
			return;
		}
		boolean canRetry = outcome.failureType() == EmailCampaignFailureType.RETRYABLE
			&& recipient.getAttemptCount() < maxAttempts;
		if (canRetry) {
			long multiplier = 1L << Math.max(0, recipient.getAttemptCount() - 1);
			recipient.markRetryableFailure(
				outcome.errorCode(), outcome.errorDetail(), now.plusSeconds(initialBackoffSeconds * multiplier));
		} else {
			recipient.markPermanentFailure(outcome.errorCode(), outcome.errorDetail());
		}
	}

	/**
	 * PENDING과 SENDING 수신자가 없으면 상태별 집계를 계산하여 캠페인을 종료한다.
	 * @param campaignId 완료 여부를 판단할 캠페인 ID
	 * @param now 완료 시각
	 * @return 캠페인을 이번 호출에서 완료했으면 true
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean completeIfTerminal(String campaignId, LocalDateTime now) {
		long pending = count(campaignId, EmailCampaignRecipientStatus.PENDING);
		long sending = count(campaignId, EmailCampaignRecipientStatus.SENDING);
		if (pending > 0 || sending > 0) {
			return false;
		}
		long sent = count(campaignId, EmailCampaignRecipientStatus.SENT);
		long failed = count(campaignId, EmailCampaignRecipientStatus.FAILED);
		long skipped = count(campaignId, EmailCampaignRecipientStatus.SKIPPED);
		getCampaign(campaignId).complete(sent, failed, skipped, now);
		return true;
	}

	/**
	 * timeout 기준보다 오래된 모든 SENDING claim을 PENDING으로 복구한다.
	 * @param claimedBefore stale 여부 기준 시각
	 * @param now 복구된 수신자의 다음 시도 가능 시각
	 * @return 복구된 수신자 수
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int releaseStaleClaims(LocalDateTime claimedBefore, LocalDateTime now) {
		return recipientRepository.releaseStaleClaims(
			EmailCampaignRecipientStatus.SENDING, EmailCampaignRecipientStatus.PENDING, claimedBefore, now);
	}

	private long count(String campaignId, EmailCampaignRecipientStatus status) {
		return recipientRepository.countByCampaignIdAndStatus(campaignId, status);
	}

	private EmailCampaign getCampaign(String campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND::toBaseException);
	}

	private EmailCampaignRecipient getRecipient(Long recipientId) {
		return recipientRepository.findById(recipientId)
			.orElseThrow(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND::toBaseException);
	}
}
