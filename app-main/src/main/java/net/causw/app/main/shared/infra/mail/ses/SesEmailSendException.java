package net.causw.app.main.shared.infra.mail.ses;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;

import lombok.Getter;

@Getter
public class SesEmailSendException extends RuntimeException {

	private final EmailCampaignFailureType failureType;
	private final String safeErrorCode;

	public SesEmailSendException(
		EmailCampaignFailureType failureType, String safeErrorCode, String message, Throwable cause) {
		super(message, cause);
		this.failureType = failureType;
		this.safeErrorCode = safeErrorCode;
	}
}
