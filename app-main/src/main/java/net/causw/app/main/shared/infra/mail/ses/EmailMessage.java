package net.causw.app.main.shared.infra.mail.ses;

public record EmailMessage(
	String from,
	String replyTo,
	String to,
	String subject,
	String sanitizedHtml) {
}
