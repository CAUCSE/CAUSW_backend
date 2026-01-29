package net.causw.app.main.domain.campus.schedule.util;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleMapper {
	public static Schedule from(ScheduleDto scheduleDto) {
		return Schedule.of(
			scheduleDto.title(),
			scheduleDto.type(),
			scheduleDto.start(),
			scheduleDto.end(),
			scheduleDto.creator());
	}

	public static ScheduleDto to(Schedule schedule) {
		return ScheduleDto.builder()
			.title(schedule.getTitle())
			.type(schedule.getType())
			.start(schedule.getStart())
			.end(schedule.getEnd())
			.creator(schedule.getCreator())
			.build();
	}

}
