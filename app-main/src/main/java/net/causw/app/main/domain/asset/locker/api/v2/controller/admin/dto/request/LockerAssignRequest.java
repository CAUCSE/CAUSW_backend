package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사물함 배정 요청")
public record LockerAssignRequest(

	@Schema(description = "배정할 사용자 ID", example = "user-uuid-1234")
	@NotBlank
	String userId) {
}
