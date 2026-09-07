package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

public record EmailCampaignRecipientListResult(
	Long id,
	String maskedEmail,
	EmailCampaignRecipientStatus status,
	int attemptCount,
	LocalDateTime lastAttemptAt,
	LocalDateTime sentAt,
	String lastErrorCode) {
}
