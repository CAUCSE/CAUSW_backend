package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record EmailCampaignCreateCommand(
	String subject,
	String html,
	EmailCampaignFilter filter,
	User creator) {
}
