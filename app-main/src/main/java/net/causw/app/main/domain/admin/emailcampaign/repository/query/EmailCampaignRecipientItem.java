package net.causw.app.main.domain.admin.emailcampaign.repository.query;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

public record EmailCampaignRecipientItem(
	Long id,
	String emailSnapshot,
	EmailCampaignRecipientStatus status,
	int attemptCount,
	LocalDateTime lastAttemptAt,
	LocalDateTime sentAt,
	String lastErrorCode) {
}
