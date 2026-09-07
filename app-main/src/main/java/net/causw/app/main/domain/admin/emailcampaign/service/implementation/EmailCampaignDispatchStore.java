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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean prepare(String campaignId, LocalDateTime now) {
		EmailCampaign campaign = getCampaign(campaignId);
		if (campaign.getStatus() == EmailCampaignStatus.QUEUED) {
			campaign.start(now);
			return true;
		}
		return campaign.getStatus() == EmailCampaignStatus.SENDING;
	}

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
