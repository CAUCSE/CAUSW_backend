package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사물함 배정 요청")
public record LockerAssignRequestDto(

	@Schema(description = "배정할 사용자 ID", example = "user-uuid-1234") @NotBlank String userId,

	@Schema(description = "만료일시 (ISO 8601, 선택)", example = "2025-06-30T23:59:59") LocalDateTime expiredAt) {
}
