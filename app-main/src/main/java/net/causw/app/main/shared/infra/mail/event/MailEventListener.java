package net.causw.app.main.shared.infra.mail.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.shared.infra.mail.GoogleMailSender;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailEventListener {

	private final GoogleMailSender googleMailSender;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEmailVerification(EmailVerificationEvent event) {
		googleMailSender.sendEmailVerificationMail(event.email(), event.verificationCode());
	}

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFindPassword(FindPasswordEvent event) {
		googleMailSender.sendNewPasswordMail(event.email(), event.newPassword());
	}
}
