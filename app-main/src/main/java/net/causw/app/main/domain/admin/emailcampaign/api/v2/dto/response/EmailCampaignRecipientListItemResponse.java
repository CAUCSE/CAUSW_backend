package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

public record EmailCampaignRecipientListItemResponse(
	Long id,
	String maskedEmail,
	EmailCampaignRecipientStatus status,
	int attemptCount,
	LocalDateTime lastAttemptAt,
	LocalDateTime sentAt,
	String lastErrorCode) {
}
