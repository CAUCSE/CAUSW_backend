package net.causw.app.main.dto.locker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerExtendDateRequestDto {
    @NotNull(message = "연장 시작일을 입력해 주세요.")
    @Schema(description = "Extend period start date", example = "2025-09-03T00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime extendStartAt;

    @NotNull(message = "연장 종료일을 입력해 주세요.")
    @Schema(description = "Extend period end date", example = "2025-09-04T23:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime extendEndAt;

    @NotNull(message = "다음 만료일을 입력해 주세요.")
    @Schema(description = "Next expiration date", example = "2025-12-31T23:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime nextExpiredAt;
}
