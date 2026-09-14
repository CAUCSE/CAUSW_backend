package net.causw.app.main.domain.admin.emailcampaign.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.event.EmailCampaignSendRequestedEvent;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignRecipientItem;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignSummary;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignTarget;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignCreateCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignFilter;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignSendCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignTargetPreviewResult;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignAuditLogWriter;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignReader;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignTargetReader;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignValidator;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignWriter;
import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailHtmlSanitizer;
import net.causw.app.main.domain.user.account.util.masking.EmailMasker;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailCampaignService {

	private final EmailCampaignTargetReader targetReader;
	private final EmailCampaignReader campaignReader;
	private final EmailCampaignWriter campaignWriter;
	private final EmailCampaignValidator validator;
	private final EmailHtmlSanitizer htmlSanitizer;
	private final EmailCampaignProperties properties;
	private final ObjectMapper objectMapper;
	private final ApplicationEventPublisher eventPublisher;
	private final EmailCampaignAuditLogWriter auditLogWriter;

	/**
	 * 발송 대상 필터를 적용하고 대상 수와 항목별 분포를 계산한다.
	 * @param filter 정규화된 대상 필터
	 * @return 대상 미리보기 결과
	 */
	public EmailCampaignTargetPreviewResult previewTargets(EmailCampaignFilter filter) {
		return toPreviewResult(targetReader.findTargets(filter));
	}

	@Transactional
	/**
	 * HTML을 정제하고 현재 대상자의 이메일 스냅샷과 함께 캠페인을 생성한다.
	 * @param command 캠페인 생성 명령
	 * @return 저장된 DRAFT 캠페인
	 */
	public EmailCampaignResult createCampaign(EmailCampaignCreateCommand command) {
		List<EmailCampaignTarget> targets = targetReader.findTargets(command.filter());
		validator.validateCreate(command.subject(), targets.size());
		String sanitizedHtml = htmlSanitizer.sanitize(command.html());
		EmailCampaign campaign = EmailCampaign.of(
			command.subject().trim(),
			sanitizedHtml,
			properties.getFromAddress(),
			properties.getReplyToAddress(),
			serializeFilter(command.filter()),
			targets.size(),
			command.creator().getId());
		EmailCampaign savedCampaign = campaignWriter.saveWithRecipients(campaign, targets);
		auditLogWriter.writeCreate(savedCampaign, command.creator());
		return toResult(savedCampaign);
	}

	@Transactional
	/**
	 * SES 설정과 최종 확인값을 검증하고 커밋 후 발송 이벤트를 발행한다.
	 * @param command 캠페인 발송 요청 명령
	 * @return QUEUED 상태의 캠페인
	 */
	public EmailCampaignResult requestSend(EmailCampaignSendCommand command) {
		validator.validateConfiguration(properties);
		EmailCampaign campaign = campaignReader.getById(command.campaignId());
		validator.validateSend(campaign, command.confirmedSubject(), command.confirmedRecipientCount());
		campaign.queue(command.requester().getId(), LocalDateTime.now());
		auditLogWriter.writeSendRequest(campaign, command.requester());
		eventPublisher.publishEvent(new EmailCampaignSendRequestedEvent(campaign.getId()));
		return toResult(campaign);
	}

	/**
	 * 조건에 해당하는 캠페인 요약 목록을 페이지 조회한다.
	 * @param query 상태와 생성 기간 조건
	 * @param pageable 페이지 정보
	 * @return 캠페인 요약 페이지
	 */
	public Page<EmailCampaignResult> getCampaigns(EmailCampaignListQuery query, Pageable pageable) {
		return campaignReader.findCampaigns(query.status(), query.from(), query.to(), pageable)
			.map(this::toResult);
	}

	/**
	 * 캠페인 상세 정보를 조회한다.
	 * @param campaignId 캠페인 ID
	 * @return 캠페인 상세 결과
	 */
	public EmailCampaignResult getCampaignDetail(String campaignId) {
		return toResult(campaignReader.getById(campaignId));
	}

	/**
	 * 캠페인 수신자 목록을 조회하고 이메일 주소를 마스킹한다.
	 * @param query 캠페인 ID와 수신자 상태 조건
	 * @param pageable 페이지 정보
	 * @return 마스킹된 수신자 처리 결과 페이지
	 */
	public Page<EmailCampaignRecipientListResult> getCampaignRecipients(
		EmailCampaignRecipientListQuery query, Pageable pageable) {
		return campaignReader.findRecipients(query.campaignId(), query.status(), pageable)
			.map(this::toResult);
	}

	private EmailCampaignTargetPreviewResult toPreviewResult(List<EmailCampaignTarget> targets) {
		return new EmailCampaignTargetPreviewResult(
			targets.size(),
			distribution(targets,
				target -> target.admissionYear() == null ? "UNKNOWN" : target.admissionYear().toString()),
			distribution(targets, target -> target.department() == null ? "UNKNOWN" : target.department().name()),
			distribution(targets,
				target -> target.academicStatus() == null ? "UNKNOWN" : target.academicStatus().name()));
	}

	private Map<String, Long> distribution(
		List<EmailCampaignTarget> targets, Function<EmailCampaignTarget, String> classifier) {
		return targets.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
	}

	private String serializeFilter(EmailCampaignFilter filter) {
		try {
			return objectMapper.writeValueAsString(filter);
		} catch (JacksonException exception) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_FILTER.toBaseException(
				"이메일 캠페인 필터를 직렬화할 수 없습니다.", exception);
		}
	}

	private EmailCampaignResult toResult(EmailCampaign campaign) {
		return new EmailCampaignResult(
			campaign.getId(), campaign.getSubject(), campaign.getSanitizedHtml(), campaign.getFilterJson(),
			campaign.getStatus(), campaign.getRecipientCount(), campaign.getPendingCount(), campaign.getSentCount(),
			campaign.getFailedCount(), campaign.getSkippedCount(), campaign.getCreatedAt(), campaign.getCompletedAt());
	}

	private EmailCampaignResult toResult(EmailCampaignSummary summary) {
		return new EmailCampaignResult(
			summary.id(), summary.subject(), null, null, summary.status(), summary.recipientCount(),
			summary.pendingCount(),
			summary.sentCount(), summary.failedCount(), summary.skippedCount(), summary.createdAt(),
			summary.completedAt());
	}

	private EmailCampaignRecipientListResult toResult(EmailCampaignRecipientItem item) {
		return new EmailCampaignRecipientListResult(
			item.id(), EmailMasker.mask(item.emailSnapshot()), item.status(), item.attemptCount(), item.lastAttemptAt(),
			item.sentAt(), item.lastErrorCode());
	}
}
