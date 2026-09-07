package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;
import net.causw.app.main.shared.infra.mail.ses.EmailMessage;
import net.causw.app.main.shared.infra.mail.ses.EmailSendResult;
import net.causw.app.main.shared.infra.mail.ses.EmailSender;
import net.causw.app.main.shared.infra.mail.ses.SesEmailSendException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailCampaignDispatcher {

	private final EmailCampaignDispatchStore dispatchStore;
	private final EmailSender emailSender;
	private final EmailCampaignRateLimiter rateLimiter;
	private final EmailCampaignProperties properties;
	private final EmailCampaignAuditLogWriter auditLogWriter;
	private final Semaphore inFlightLimit;
	private final Set<String> activeCampaigns = ConcurrentHashMap.newKeySet();

	public EmailCampaignDispatcher(
		EmailCampaignDispatchStore dispatchStore,
		EmailSender emailSender,
		EmailCampaignRateLimiter rateLimiter,
		EmailCampaignProperties properties,
		EmailCampaignAuditLogWriter auditLogWriter) {
		this.dispatchStore = dispatchStore;
		this.emailSender = emailSender;
		this.rateLimiter = rateLimiter;
		this.properties = properties;
		this.auditLogWriter = auditLogWriter;
		this.inFlightLimit = new Semaphore(Math.max(1, properties.getMaxConcurrency()));
	}

	public void dispatch(String campaignId) {
		if (!properties.isEnabled() || !activeCampaigns.add(campaignId)) {
			return;
		}
		try {
			if (!dispatchStore.prepare(campaignId, LocalDateTime.now())) {
				return;
			}
			while (dispatchBatch(campaignId)) {
				// Continue while immediately dispatchable recipients remain.
			}
			if (dispatchStore.completeIfTerminal(campaignId, LocalDateTime.now())) {
				auditLogWriter.writeCompletion(campaignId);
			}
		} catch (RuntimeException exception) {
			log.error("Email campaign dispatch failed. campaignId={}", campaignId, exception);
		} finally {
			activeCampaigns.remove(campaignId);
		}
	}

	private boolean dispatchBatch(String campaignId) {
		List<EmailCampaignDispatchTarget> targets = dispatchStore.claimBatch(
			campaignId, properties.getBatchSize(), LocalDateTime.now());
		if (targets.isEmpty()) {
			return false;
		}
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<EmailCampaignSendOutcome>> futures = targets.stream()
				.map(target -> executor.submit(() -> send(target)))
				.toList();
			for (Future<EmailCampaignSendOutcome> future : futures) {
				dispatchStore.recordOutcome(
					await(future), properties.getMaxAttempts(), properties.getInitialBackoffSeconds(),
					LocalDateTime.now());
			}
		}
		return targets.size() == properties.getBatchSize();
	}

	private EmailCampaignSendOutcome send(EmailCampaignDispatchTarget target) {
		rateLimiter.acquire();
		boolean acquired = false;
		try {
			inFlightLimit.acquire();
			acquired = true;
			EmailSendResult result = emailSender.send(new EmailMessage(
				target.from(), target.replyTo(), target.to(), target.subject(), target.sanitizedHtml()));
			return EmailCampaignSendOutcome.success(target.recipientId(), result.messageId());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return EmailCampaignSendOutcome.failure(
				target.recipientId(), EmailCampaignFailureType.RETRYABLE, "DISPATCH_INTERRUPTED", "발송 작업이 중단되었습니다.");
		} catch (SesEmailSendException exception) {
			return EmailCampaignSendOutcome.failure(
				target.recipientId(), exception.getFailureType(), exception.getSafeErrorCode(), exception.getMessage());
		} catch (RuntimeException exception) {
			log.error("Unexpected email send failure. recipientId={}", target.recipientId(), exception);
			return EmailCampaignSendOutcome.failure(
				target.recipientId(), EmailCampaignFailureType.RETRYABLE, "UNEXPECTED_SEND_ERROR",
				"이메일 발송 중 오류가 발생했습니다.");
		} finally {
			if (acquired) {
				inFlightLimit.release();
			}
		}
	}

	private EmailCampaignSendOutcome await(Future<EmailCampaignSendOutcome> future) {
		try {
			return future.get();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("이메일 캠페인 발송 결과 대기가 중단되었습니다.", exception);
		} catch (ExecutionException exception) {
			throw new IllegalStateException("이메일 캠페인 발송 결과를 처리할 수 없습니다.", exception.getCause());
		}
	}
}
