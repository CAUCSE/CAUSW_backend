package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LockerPolicyExtendStatusRequest(
        @Schema(description = "연장 가능 여부(상태)", example = "false") @NotNull boolean status
) {
}
