package net.causw.app.main.domain.admin.emailcampaign.repository.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

public record EmailCampaignSummary(
	String id,
	String subject,
	EmailCampaignStatus status,
	long recipientCount,
	long pendingCount,
	long sentCount,
	long failedCount,
	long skippedCount,
	LocalDateTime createdAt,
	LocalDateTime completedAt) {
}
