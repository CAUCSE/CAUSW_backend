package net.causw.app.main.domain.campus.schedule.service.v2.implementation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.repository.ScheduleRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleReader {
	private final ScheduleRepository scheduleRepository;

	/**
	 * Schedule ID로 Schedule을 조회합니다.
	 *
	 * @param scheduleId Schedule ID
	 * @return Schedule Entity
	 * @throws BadRequestException Schedule을 찾을 수 없는 경우
	 */
	public Schedule findById(String scheduleId) {
		return scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.SCHEDULE_NOT_FOUND));
	}

	/**
	 * 특정 기간 내의 Schedule을 조회합니다.
	 * 일정이 기간과 겹치기만 하면 조회됩니다 (완전히 포함될 필요 없음).
	 *
	 * @param from 시작 날짜
	 * @param to 종료 날짜
	 * @param types Schedule 타입 리스트 (null 또는 빈 리스트일 경우 모든 타입 조회)
	 * @return Schedule 목록
	 */
	public List<Schedule> findByCondition(LocalDateTime from, LocalDateTime to, List<ScheduleType> types) {
		return scheduleRepository.findAllByCondition(from, to, types);
	}
}
