package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;

public record EmailCampaignSendOutcome(
	Long recipientId,
	String messageId,
	EmailCampaignFailureType failureType,
	String errorCode,
	String errorDetail) {

	public static EmailCampaignSendOutcome success(Long recipientId, String messageId) {
		return new EmailCampaignSendOutcome(recipientId, messageId, null, null, null);
	}

	public static EmailCampaignSendOutcome failure(
		Long recipientId, EmailCampaignFailureType failureType, String errorCode, String errorDetail) {
		return new EmailCampaignSendOutcome(recipientId, null, failureType, errorCode, errorDetail);
	}

	public boolean isSuccess() {
		return messageId != null;
	}
}
