package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EmailCampaignSendRequest(
	@NotBlank String confirmedSubject,
	@Positive long confirmedRecipientCount) {
}
