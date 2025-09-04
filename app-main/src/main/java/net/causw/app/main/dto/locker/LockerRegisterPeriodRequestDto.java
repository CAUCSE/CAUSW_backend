package net.causw.app.main.dto.locker;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerRegisterPeriodRequestDto {
	@NotNull(message = "신청 시작일을 입력해 주세요.")
	@Schema(description = "Register period start date", example = "2025-09-08T00:00", requiredMode = Schema.RequiredMode.REQUIRED)
	private LocalDateTime registerStartAt;

	@NotNull(message = "신청 종료일을 입력해 주세요.")
	@Schema(description = "Register period end date", example = "2025-09-09T23:59", requiredMode = Schema.RequiredMode.REQUIRED)
	private LocalDateTime registerEndAt;
}
