package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleCalendarDto;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleMaskingResolver;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;
import net.causw.app.main.domain.campus.schedule.util.ScheduleMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyMapper;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleWriter scheduleWriter;
	private final ScheduleReader scheduleReader;
	private final ScheduleMaskingResolver scheduleMaskingResolver;
	private final CeremonyReader ceremonyReader;

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
	public List<ScheduleDto> findByCondition(LocalDateTime from, LocalDateTime to, Collection<ScheduleType> types) {
		return scheduleReader.findByCondition(from, to, types).stream().map(ScheduleMapper::to)
			.toList();
	}

	@Transactional(readOnly = true)
	public ScheduleDto findById(String scheduleId) {
		return ScheduleMapper.to(scheduleReader.findById(scheduleId));
	}

	/**
	 * 조건에 따라 일정을 조회하고, 연결된 게시물(targetPostId)에 대한 읽기 권한이 없는 경우 null로 마스킹합니다.
	 *
	 * @param from   시작 일시
	 * @param to     종료 일시
	 * @param categories 일정 카테고리 필터
	 * @param viewer 현재 요청 사용자 (null이면 모두 마스킹)
	 * @return targetPostId가 권한에 따라 마스킹된 일정 목록
	 */
	@Transactional(readOnly = true)
	public List<ScheduleCalendarDto> findByConditionWithMasking(
		LocalDateTime from,
		LocalDateTime to,
		Collection<ScheduleCategory> categories,
		User viewer) {

		List<ScheduleType> scheduleTypes = categories == null
			? null
			: categories.stream()
				.filter(category -> category != ScheduleCategory.CEREMONY)
				.map(category -> ScheduleType.valueOf(category.name()))
				.toList();

		List<ScheduleDto> scheduleDtos;

		if (categories != null && scheduleTypes.isEmpty()) {
			scheduleDtos = List.of();
		} else {
			scheduleDtos = scheduleReader.findByCondition(from, to, scheduleTypes).stream()
				.map(ScheduleMapper::to)
				.toList();
		}

		Set<String> readablePostIds = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

		List<ScheduleCalendarDto> schedules = scheduleDtos.stream()
			.map(dto -> scheduleMaskingResolver.maskIfUnreadable(dto, readablePostIds))
			.map(this::toCalendarDto)
			.toList();

		boolean includeCeremony = categories == null || categories.contains(ScheduleCategory.CEREMONY);

		List<ScheduleCalendarDto> ceremonies = includeCeremony
			? ceremonyReader.findAcceptedByDateRange(from.toLocalDate(), to.toLocalDate()).stream()
				.map(this::toCalendarDto)
				.toList()
			: List.of();

		return Stream.concat(schedules.stream(), ceremonies.stream())
			.sorted(
				Comparator.comparing(ScheduleCalendarDto::start)
					.thenComparing(ScheduleCalendarDto::end))
			.toList();
	}

	/**
	 * 특정 ID의 일정을 조회하고, 연결된 게시물(targetPostId)에 대한 읽기 권한이 없는 경우 null로 마스킹합니다.
	 *
	 * @param scheduleId 일정 ID
	 * @param viewer     현재 요청 사용자 (null이면 마스킹)
	 * @return targetPostId가 권한에 따라 마스킹된 일정
	 */
	@Transactional(readOnly = true)
	public ScheduleDto findByIdWithMasking(String scheduleId, User viewer) {
		ScheduleDto scheduleDto = ScheduleMapper.to(scheduleReader.findById(scheduleId));
		Set<String> readablePostIds = scheduleMaskingResolver.resolveReadablePostIds(List.of(scheduleDto), viewer);
		return scheduleMaskingResolver.maskIfUnreadable(scheduleDto, readablePostIds);
	}

	private ScheduleCalendarDto toCalendarDto(ScheduleDto dto) {
		return ScheduleCalendarDto.builder()
			.id(dto.id())
			.title(dto.title())
			.type(ScheduleCategory.from(dto.type()))
			.start(dto.start())
			.end(dto.end())
			.targetPostId(dto.targetPostId())
			.build();
	}

	private ScheduleCalendarDto toCalendarDto(Ceremony ceremony) {
		LocalDate endDate = ceremony.getEndDate() != null
			? ceremony.getEndDate()
			: ceremony.getStartDate();

		LocalTime startTime = ceremony.getStartTime() != null
			? ceremony.getStartTime()
			: LocalTime.MIN;

		LocalTime endTime = ceremony.getEndTime() != null
			? ceremony.getEndTime()
			: LocalTime.of(23, 59, 59);

		return ScheduleCalendarDto.builder()
			.id(ceremony.getId())
			.title(CeremonyMapper.mapTitle(ceremony))
			.type(ScheduleCategory.CEREMONY)
			.start(ceremony.getStartDate().atTime(startTime))
			.end(endDate.atTime(endTime))
			.targetPostId(null)
			.build();
	}
}
