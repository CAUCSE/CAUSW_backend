package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;
import net.causw.app.main.shared.infra.mail.ses.EmailSendResult;
import net.causw.app.main.shared.infra.mail.ses.EmailSender;
import net.causw.app.main.shared.infra.mail.ses.SesEmailSendException;

class EmailCampaignDispatcherTest {

	private EmailCampaignDispatchStore dispatchStore;
	private EmailSender emailSender;
	private EmailCampaignRateLimiter rateLimiter;
	private EmailCampaignAuditLogWriter auditLogWriter;
	private EmailCampaignDispatcher dispatcher;

	@BeforeEach
	void setUp() {
		dispatchStore = mock(EmailCampaignDispatchStore.class);
		emailSender = mock(EmailSender.class);
		rateLimiter = mock(EmailCampaignRateLimiter.class);
		auditLogWriter = mock(EmailCampaignAuditLogWriter.class);
		EmailCampaignProperties properties = new EmailCampaignProperties();
		properties.setEnabled(true);
		properties.setBatchSize(10);
		properties.setMaxConcurrency(2);
		properties.setMaxAttempts(3);
		dispatcher = new EmailCampaignDispatcher(
			dispatchStore, emailSender, rateLimiter, properties, auditLogWriter);
	}

	@Test
	@DisplayName("claim된 수신자만 SES로 발송하고 성공 결과를 기록한다")
	void dispatchSuccess() {
		// given
		String campaignId = "campaign-id";
		EmailCampaignDispatchTarget target = target(1L);
		given(dispatchStore.prepare(org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.any()))
			.willReturn(true);
		given(dispatchStore.claimBatch(
			org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.eq(10),
			org.mockito.ArgumentMatchers.any())).willReturn(List.of(target));
		given(emailSender.send(org.mockito.ArgumentMatchers.any())).willReturn(new EmailSendResult("message-id"));
		given(dispatchStore.completeIfTerminal(
			org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.any())).willReturn(true);

		// when
		dispatcher.dispatch(campaignId);

		// then
		InOrder order = inOrder(rateLimiter, emailSender);
		order.verify(rateLimiter).acquire();
		order.verify(emailSender).send(org.mockito.ArgumentMatchers.any());
		ArgumentCaptor<EmailCampaignSendOutcome> captor = ArgumentCaptor.forClass(EmailCampaignSendOutcome.class);
		verify(dispatchStore).recordOutcome(
			captor.capture(), org.mockito.ArgumentMatchers.eq(3), org.mockito.ArgumentMatchers.anyLong(),
			org.mockito.ArgumentMatchers.any(LocalDateTime.class));
		assertThat(captor.getValue().messageId()).isEqualTo("message-id");
		verify(auditLogWriter).writeCompletion(campaignId);
	}

	@Test
	@DisplayName("SES 일시 오류는 재시도 가능한 결과로 기록한다")
	void dispatchRetryableFailure() {
		// given
		String campaignId = "campaign-id";
		given(dispatchStore.prepare(org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.any()))
			.willReturn(true);
		given(dispatchStore.claimBatch(
			org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.eq(10),
			org.mockito.ArgumentMatchers.any())).willReturn(List.of(target(1L)));
		given(emailSender.send(org.mockito.ArgumentMatchers.any())).willThrow(new SesEmailSendException(
			EmailCampaignFailureType.RETRYABLE, "SES_THROTTLED", "발송 실패", null));

		// when
		dispatcher.dispatch(campaignId);

		// then
		ArgumentCaptor<EmailCampaignSendOutcome> captor = ArgumentCaptor.forClass(EmailCampaignSendOutcome.class);
		verify(dispatchStore).recordOutcome(
			captor.capture(), org.mockito.ArgumentMatchers.eq(3), org.mockito.ArgumentMatchers.anyLong(),
			org.mockito.ArgumentMatchers.any(LocalDateTime.class));
		assertThat(captor.getValue().failureType()).isEqualTo(EmailCampaignFailureType.RETRYABLE);
		assertThat(captor.getValue().errorCode()).isEqualTo("SES_THROTTLED");
	}

	@Test
	@DisplayName("같은 캠페인의 dispatcher가 실행 중이면 중복 실행하지 않는다")
	void preventDuplicateDispatch() throws InterruptedException {
		// given
		String campaignId = "campaign-id";
		CountDownLatch sending = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		given(dispatchStore.prepare(org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.any()))
			.willReturn(true);
		given(dispatchStore.claimBatch(
			org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.eq(10),
			org.mockito.ArgumentMatchers.any())).willReturn(List.of(target(1L)));
		given(emailSender.send(org.mockito.ArgumentMatchers.any())).willAnswer(invocation -> {
			sending.countDown();
			release.await(3, TimeUnit.SECONDS);
			return new EmailSendResult("message-id");
		});
		Thread firstDispatch = Thread.ofPlatform().start(() -> dispatcher.dispatch(campaignId));
		assertThat(sending.await(3, TimeUnit.SECONDS)).isTrue();

		// when
		dispatcher.dispatch(campaignId);
		release.countDown();
		firstDispatch.join(3000);

		// then
		verify(dispatchStore, times(1)).prepare(
			org.mockito.ArgumentMatchers.eq(campaignId), org.mockito.ArgumentMatchers.any());
	}

	private EmailCampaignDispatchTarget target(Long recipientId) {
		return new EmailCampaignDispatchTarget(
			recipientId, "from@example.com", "reply@example.com", "to@example.com", "제목", "<p>본문</p>", 0);
	}
}
