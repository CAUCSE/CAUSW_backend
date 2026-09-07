package net.causw.app.main.domain.admin.emailcampaign.api.v2.mapper;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignCreateRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignFilterRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignRecipientListRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request.EmailCampaignSendRequest;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignRecipientListItemResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignResponse;
import net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response.EmailCampaignTargetPreviewResponse;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignCreateCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignFilter;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListQuery;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignRecipientListResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignResult;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignSendCommand;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignTargetPreviewResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Component
public class EmailCampaignApiMapper {

	public EmailCampaignFilter toFilter(EmailCampaignFilterRequest request) {
		return new EmailCampaignFilter(request.admissionYears(), request.departments(), request.academicStatuses());
	}

	public EmailCampaignCreateCommand toCommand(EmailCampaignCreateRequest request, User creator) {
		return new EmailCampaignCreateCommand(
			request.subject(), request.html(), toFilter(request.filter()), creator);
	}

	public EmailCampaignSendCommand toCommand(
		String campaignId, EmailCampaignSendRequest request, User requester) {
		return new EmailCampaignSendCommand(
			campaignId, request.confirmedSubject(), request.confirmedRecipientCount(), requester);
	}

	public EmailCampaignListQuery toQuery(EmailCampaignListRequest request) {
		return new EmailCampaignListQuery(request.status(), request.from(), request.to());
	}

	public EmailCampaignRecipientListQuery toQuery(String campaignId, EmailCampaignRecipientListRequest request) {
		return new EmailCampaignRecipientListQuery(campaignId, request.status());
	}

	public EmailCampaignTargetPreviewResponse toResponse(EmailCampaignTargetPreviewResult result) {
		return new EmailCampaignTargetPreviewResponse(
			result.recipientCount(), result.admissionYearDistribution(), result.departmentDistribution(),
			result.academicStatusDistribution());
	}

	public EmailCampaignResponse toResponse(EmailCampaignResult result) {
		return new EmailCampaignResponse(
			result.id(), result.subject(), result.sanitizedHtml(), result.filterJson(), result.status(),
			result.recipientCount(), result.pendingCount(), result.sentCount(), result.failedCount(),
			result.skippedCount(), result.createdAt(), result.completedAt());
	}

	public EmailCampaignRecipientListItemResponse toResponse(EmailCampaignRecipientListResult result) {
		return new EmailCampaignRecipientListItemResponse(
			result.id(), result.maskedEmail(), result.status(), result.attemptCount(), result.lastAttemptAt(),
			result.sentAt(), result.lastErrorCode());
	}
}
