package net.causw.app.main.domain.campus.schedule.api.v2.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "일정 생성/수정 요청")
public record ScheduleRequest(
	@Schema(description = "일정 제목", example = "중간고사 기간", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "일정 제목은 필수입니다.") @Size(min = 1, max = 100, message = "일정 제목은 1자 이상 100자 이하여야 합니다.") String title,

	@Schema(description = "일정 유형", example = "ACADEMIC", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "일정 타입은 필수입니다.") ScheduleType type,

	@Schema(description = "일정 시작 시간", example = "2026-04-15T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "시작 시간은 필수입니다.") LocalDateTime start,

	@Schema(description = "일정 종료 시간", example = "2026-04-21T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "종료 시간은 필수입니다.") LocalDateTime end) {

	@AssertTrue(message = "종료시간은 시작시간 이후여야 합니다.")
	private boolean isValidTimeRange() {
		return start == null || end == null || end.isAfter(start);
	}
}
