package net.causw.app.main.shared.infra.mail.ses;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sesv2.model.AccountSuspendedException;
import software.amazon.awssdk.services.sesv2.model.BadRequestException;
import software.amazon.awssdk.services.sesv2.model.MailFromDomainNotVerifiedException;
import software.amazon.awssdk.services.sesv2.model.MessageRejectedException;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;
import software.amazon.awssdk.services.sesv2.model.TooManyRequestsException;

@Component
public class SesEmailExceptionClassifier {

	public SesEmailSendException classify(RuntimeException exception) {
		if (exception instanceof TooManyRequestsException) {
			return create(EmailCampaignFailureType.RETRYABLE, "SES_THROTTLED", exception);
		}
		if (exception instanceof SdkClientException) {
			return create(EmailCampaignFailureType.UNKNOWN, "SES_CLIENT_UNKNOWN", exception);
		}
		if (exception instanceof MessageRejectedException
			|| exception instanceof MailFromDomainNotVerifiedException
			|| exception instanceof AccountSuspendedException
			|| exception instanceof BadRequestException) {
			return create(EmailCampaignFailureType.PERMANENT, "SES_REJECTED", exception);
		}
		if (exception instanceof SesV2Exception sesException && sesException.statusCode() >= 500) {
			return create(EmailCampaignFailureType.RETRYABLE, "SES_SERVER_ERROR", exception);
		}
		return create(EmailCampaignFailureType.PERMANENT, "SES_SEND_FAILED", exception);
	}

	private SesEmailSendException create(
		EmailCampaignFailureType failureType, String safeErrorCode, RuntimeException cause) {
		return new SesEmailSendException(failureType, safeErrorCode, "SES 이메일 발송에 실패했습니다.", cause);
	}
}
