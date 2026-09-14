package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

public record EmailCampaignRecipientListQuery(
	String campaignId,
	EmailCampaignRecipientStatus status) {
}
