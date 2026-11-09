package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

public record EmailCampaignResponse(
	String id,
	String subject,
	String sanitizedHtml,
	String filterJson,
	EmailCampaignStatus status,
	long recipientCount,
	long pendingCount,
	long sentCount,
	long failedCount,
	long skippedCount,
	LocalDateTime createdAt,
	LocalDateTime completedAt) {
}
