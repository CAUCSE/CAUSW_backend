package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

public record EmailCampaignListRequest(
	EmailCampaignStatus status,
	LocalDateTime from,
	LocalDateTime to) {
}
