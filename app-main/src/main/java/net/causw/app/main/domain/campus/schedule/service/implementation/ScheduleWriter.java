package net.causw.app.main.domain.campus.schedule.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.repository.ScheduleRepository;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ScheduleWriter {
	private final ScheduleRepository scheduleRepository;
	private final PostReader postReader;

	/**
	 * 새로운 Schedule을 생성합니다.
	 *
	 * @param dto 일정 생성 DTO
	 * @return 생성된 Schedule Entity
	 */
	public Schedule create(ScheduleDto dto) {
		postReader.findById(dto.targetPostId()); // post가 존재하는지 검증하기 위함
		Schedule schedule = ScheduleMapper.from(dto);
		return scheduleRepository.save(schedule);
	}

	/**
	 * 기존 Schedule을 삭제합니다.
	 *
	 * @param schedule 삭제할 Schedule Entity
	 */
	public void delete(Schedule schedule) {
		scheduleRepository.delete(schedule);
	}

	/**
	 * Schedule ID로 Schedule을 삭제합니다.
	 *
	 * @param scheduleId 삭제할 Schedule ID
	 */
	public void deleteById(String scheduleId) {
		scheduleRepository.deleteById(scheduleId);
	}

	/**
	 * Schedule을 update (PUT) 합니다.
	 * @param schedule 수정할 Schedule
	 * @param dto 업데이트될 정보
	 */
	public Schedule update(Schedule schedule, ScheduleDto dto) {
		postReader.findById(dto.targetPostId()); // post가 존재하는지 검증하기 위함
		schedule.update(dto.title(),
			dto.type(),
			dto.start(),
			dto.end(),
			dto.creator(),
			dto.targetPostId());
		return scheduleRepository.save(schedule);
	}
}
