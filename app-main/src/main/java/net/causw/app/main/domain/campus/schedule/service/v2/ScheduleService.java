package net.causw.app.main.domain.campus.schedule.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.v2.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.v2.implementation.ScheduleWriter;
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
}
