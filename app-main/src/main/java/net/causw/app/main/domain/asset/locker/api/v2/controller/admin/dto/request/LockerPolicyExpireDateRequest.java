package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사물함 만료일 설정 요청")
public record LockerPolicyExpireDateRequest(

	@Schema(description = "만료일시 (ISO 8601)", example = "2026-08-31T23:59:59") @NotNull LocalDateTime expiredAt) {
}
