package net.causw.app.main.domain.campus.schedule.service.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.Builder;

@Builder
public record ScheduleDto(
	String title,
	ScheduleType type,
	LocalDateTime start,
	LocalDateTime end,
	User creator) {
}
