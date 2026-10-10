package net.causw.app.main.domain.campus.schedule.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;

import lombok.Builder;

@Builder
public record ScheduleCalendarDto(
	String id,
	String title,
	ScheduleCategory type,
	LocalDateTime start,
	LocalDateTime end,
	String targetPostId) {
}