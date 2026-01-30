package net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScheduleRequest(
	@NotBlank(message = "일정 제목은 필수입니다.") @Size(min = 1, max = 100, message = "일정 제목은 1자 이상 100자 이하여야 합니다.") String title,

	@NotNull(message = "일정 타입은 필수입니다.") ScheduleType type,

	@NotNull(message = "시작 시간은 필수입니다.") LocalDateTime start,

	@NotNull(message = "종료 시간은 필수입니다.") LocalDateTime end) {

	@AssertTrue(message = "종료시간은 시작시간 이후여야 합니다.")
	private boolean isValidTimeRange() {
		return start == null || end == null || end.isAfter(start);
	}
}
