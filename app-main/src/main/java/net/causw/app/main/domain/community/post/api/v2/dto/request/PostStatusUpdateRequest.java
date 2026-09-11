package net.causw.app.main.domain.community.post.api.v2.dto.request;

import net.causw.app.main.domain.community.post.enums.PostAdminStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(
	@NotNull @Schema(description = "변경할 상태", example = "HIDDEN") PostAdminStatus status) {
}
