package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleWriter scheduleWriter;
	private final ScheduleReader scheduleReader;

	@Transactional
	public ScheduleDto save(ScheduleDto dto) {
		return ScheduleMapper.to(scheduleWriter.create(dto));
	}

	@Transactional
	public ScheduleDto update(String scheduleId, ScheduleDto dto) {
		Schedule schedule = scheduleReader.findById(scheduleId);
		return ScheduleMapper.to(scheduleWriter.update(schedule, dto));
	}

	@Transactional
	public void delete(String scheduleId) {
		scheduleWriter.deleteById(scheduleId);
	}

	@Transactional(readOnly = true)
	public List<ScheduleDto> findByCondition(LocalDateTime from, LocalDateTime to, List<ScheduleType> types) {
		return scheduleReader.findByCondition(from, to, types).stream().map(ScheduleMapper::to).toList();
	}

	@Transactional(readOnly = true)
	public ScheduleDto findById(String scheduleId) {
		return ScheduleMapper.to(scheduleReader.findById(scheduleId));
	}
}
