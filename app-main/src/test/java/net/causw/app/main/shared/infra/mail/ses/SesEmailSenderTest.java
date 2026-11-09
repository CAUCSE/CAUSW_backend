package net.causw.app.main.shared.infra.mail.ses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

class SesEmailSenderTest {

	@Test
	@DisplayName("SES v2 요청에 발신자 수신자 제목 HTML과 회신 주소를 담는다")
	void send() {
		// given
		SesV2Client client = mock(SesV2Client.class);
		ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
		given(client.sendEmail(captor.capture()))
			.willReturn(SendEmailResponse.builder().messageId("message-id").build());
		SesEmailSender sender = new SesEmailSender(client, new SesEmailExceptionClassifier());

		// when
		EmailSendResult result = sender.send(new EmailMessage(
			"from@example.com", "reply@example.com", "to@example.com", "제목", "<p>본문</p>"));

		// then
		SendEmailRequest request = captor.getValue();
		assertThat(result.messageId()).isEqualTo("message-id");
		assertThat(request.fromEmailAddress()).isEqualTo("from@example.com");
		assertThat(request.destination().toAddresses()).containsExactly("to@example.com");
		assertThat(request.replyToAddresses()).containsExactly("reply@example.com");
		assertThat(request.content().simple().subject().data()).isEqualTo("제목");
		assertThat(request.content().simple().body().html().data()).isEqualTo("<p>본문</p>");
	}
}
