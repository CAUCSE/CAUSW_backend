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

	public EmailCampaignTargetPreviewResult previewTargets(EmailCampaignFilter filter) {
		return toPreviewResult(targetReader.findTargets(filter));
	}

	@Transactional
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
	public EmailCampaignResult requestSend(EmailCampaignSendCommand command) {
		validator.validateConfiguration(properties);
		EmailCampaign campaign = campaignReader.getById(command.campaignId());
		validator.validateSend(campaign, command.confirmedSubject(), command.confirmedRecipientCount());
		campaign.queue(command.requester().getId(), LocalDateTime.now());
		auditLogWriter.writeSendRequest(campaign, command.requester());
		eventPublisher.publishEvent(new EmailCampaignSendRequestedEvent(campaign.getId()));
		return toResult(campaign);
	}

	public Page<EmailCampaignResult> getCampaigns(EmailCampaignListQuery query, Pageable pageable) {
		return campaignReader.findCampaigns(query.status(), query.from(), query.to(), pageable)
			.map(this::toResult);
	}

	public EmailCampaignResult getCampaignDetail(String campaignId) {
		return toResult(campaignReader.getById(campaignId));
	}

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
