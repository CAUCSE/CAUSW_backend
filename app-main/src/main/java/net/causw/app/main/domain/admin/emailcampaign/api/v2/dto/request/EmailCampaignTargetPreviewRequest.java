package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EmailCampaignTargetPreviewRequest(
	@Valid @NotNull EmailCampaignFilterRequest filter) {
}
