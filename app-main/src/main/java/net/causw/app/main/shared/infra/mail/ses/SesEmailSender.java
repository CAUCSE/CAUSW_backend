package net.causw.app.main.shared.infra.mail.ses;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@Component
@RequiredArgsConstructor
public class SesEmailSender implements EmailSender {

	private final SesV2Client sesV2Client;
	private final SesEmailExceptionClassifier exceptionClassifier;

	/**
	 * 메시지를 SES v2 SendEmail 요청으로 변환하여 발송하고 message ID를 반환한다.
	 * @param message 정제된 HTML 이메일 메시지
	 * @return SES 접수 message ID
	 */
	@Override
	public EmailSendResult send(EmailMessage message) {
		try {
			SendEmailResponse response = sesV2Client.sendEmail(toRequest(message));
			return new EmailSendResult(response.messageId());
		} catch (RuntimeException exception) {
			throw exceptionClassifier.classify(exception);
		}
	}

	private SendEmailRequest toRequest(EmailMessage message) {
		Content subject = Content.builder().data(message.subject()).charset("UTF-8").build();
		Content html = Content.builder().data(message.sanitizedHtml()).charset("UTF-8").build();
		Message content = Message.builder().subject(subject).body(Body.builder().html(html).build()).build();
		SendEmailRequest.Builder builder = SendEmailRequest.builder()
			.fromEmailAddress(message.from())
			.destination(Destination.builder().toAddresses(message.to()).build())
			.content(EmailContent.builder().simple(content).build());
		if (message.replyTo() != null && !message.replyTo().isBlank()) {
			builder.replyToAddresses(message.replyTo());
		}
		return builder.build();
	}
}
