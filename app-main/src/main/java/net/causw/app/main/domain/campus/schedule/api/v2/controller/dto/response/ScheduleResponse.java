package net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

import lombok.Builder;

@Builder
public record ScheduleResponse(
	String id,
	String title,
	ScheduleType type,
	LocalDateTime start,
	LocalDateTime end) {
}
