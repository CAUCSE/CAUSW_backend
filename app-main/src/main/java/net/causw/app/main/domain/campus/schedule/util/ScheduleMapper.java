package net.causw.app.main.domain.campus.schedule.util;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;

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
			scheduleDto.creator(),
			scheduleDto.targetPostId());
	}

	public static ScheduleDto to(Schedule schedule) {
		return ScheduleDto.builder()
			.id(schedule.getId())
			.title(schedule.getTitle())
			.type(schedule.getType())
			.start(schedule.getStart())
			.end(schedule.getEnd())
			.creator(schedule.getCreator())
			.targetPostId(schedule.getTargetPostId())
			.build();
	}

	/**
	 * targetPostId만 null로 마스킹한 새 ScheduleDto를 반환합니다.
	 *
	 * @param dto 원본 일정 DTO
	 * @return targetPostId가 null인 새 ScheduleDto
	 */
	public static ScheduleDto toWithoutTargetPost(ScheduleDto dto) {
		return ScheduleDto.builder()
			.id(dto.id())
			.title(dto.title())
			.type(dto.type())
			.start(dto.start())
			.end(dto.end())
			.creator(dto.creator())
			.targetPostId(null)
			.build();
	}

}
