package net.causw.app.main.domain.community.systemnotice.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemNoticeUpdateRequest(
	@NotBlank @Size(max = 255) String title,
	@NotBlank String content) {
}
