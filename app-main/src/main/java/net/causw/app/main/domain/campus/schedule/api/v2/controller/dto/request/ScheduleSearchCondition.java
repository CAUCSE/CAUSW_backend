package net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;

@Builder
public record ScheduleSearchCondition(
	LocalDateTime from,
	LocalDateTime to,
	ScheduleType type) {

	@AssertTrue(message = "종료시간은 시작시간 이후여야 합니다.")
	private boolean isValidTimeRange() {
		return from == null || to == null || to.isAfter(from);
	}
}
