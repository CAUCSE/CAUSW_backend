package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

@Component
public class EmailCampaignValidator {

	public void validateCreate(String subject, int targetCount) {
		if (subject == null || subject.isBlank() || subject.length() > 255) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_SUBJECT.toBaseException();
		}
		if (targetCount == 0) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_RECIPIENT_EMPTY.toBaseException();
		}
	}

	public void validateSend(EmailCampaign campaign, String confirmedSubject, long confirmedRecipientCount) {
		if (campaign.getStatus() != EmailCampaignStatus.DRAFT) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED.toBaseException();
		}
		if (!campaign.getSubject().equals(confirmedSubject)
			|| campaign.getRecipientCount() != confirmedRecipientCount) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH.toBaseException();
		}
	}

	public void validateConfiguration(EmailCampaignProperties properties) {
		if (!properties.isEnabled()) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_DISABLED.toBaseException();
		}
		if (properties.getFromAddress() == null || properties.getFromAddress().isBlank()
			|| properties.getMaxSendRate() <= 0 || properties.getMaxConcurrency() <= 0
			|| properties.getBatchSize() <= 0 || properties.getMaxAttempts() <= 0) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_CONFIGURATION_INVALID.toBaseException();
		}
	}
}
