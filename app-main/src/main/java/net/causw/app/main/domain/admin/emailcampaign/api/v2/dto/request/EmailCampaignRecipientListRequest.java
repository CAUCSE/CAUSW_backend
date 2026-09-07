package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;

public record EmailCampaignRecipientListRequest(
	EmailCampaignRecipientStatus status) {
}
