package net.causw.app.main.shared.infra.mail.ses;

public interface EmailSender {

	EmailSendResult send(EmailMessage message);
}
