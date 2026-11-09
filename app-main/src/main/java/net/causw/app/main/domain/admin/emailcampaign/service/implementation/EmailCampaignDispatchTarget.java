package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

public record EmailCampaignDispatchTarget(
	Long recipientId,
	String from,
	String replyTo,
	String to,
	String subject,
	String sanitizedHtml,
	int attemptCount) {
}
