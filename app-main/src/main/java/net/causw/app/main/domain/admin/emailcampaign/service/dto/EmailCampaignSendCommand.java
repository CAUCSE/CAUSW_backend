package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

public record EmailCampaignSendCommand(
	String campaignId,
	String confirmedSubject,
	long confirmedRecipientCount,
	User requester) {
}
