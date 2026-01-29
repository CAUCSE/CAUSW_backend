package net.causw.app.main.domain.campus.schedule.util;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleCreateDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleMapper {
	public static Schedule fromCreateDto(ScheduleCreateDto scheduleCreateDto) {
		return Schedule.of(
			scheduleCreateDto.title(),
			scheduleCreateDto.type(),
			scheduleCreateDto.start(),
			scheduleCreateDto.end(),
			scheduleCreateDto.creator());
	}
}
