package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailCampaignCreateRequest(
	@NotBlank @Size(max = 255) String subject,
	@NotBlank String html,
	@Valid @NotNull EmailCampaignFilterRequest filter) {
}
