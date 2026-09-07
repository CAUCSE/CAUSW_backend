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
