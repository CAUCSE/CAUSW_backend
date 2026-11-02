package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

public record EmailCampaignListQuery(
	EmailCampaignStatus status,
	LocalDateTime from,
	LocalDateTime to) {
}
