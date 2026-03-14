package net.causw.app.main.domain.campus.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.repository.ScheduleRepository;
import net.causw.app.main.domain.campus.schedule.service.HolidayApiClient.HolidayInfo;

@ExtendWith(MockitoExtension.class)
class HolidayScheduleSyncServiceTest {

	@Mock
	private HolidayApiClient holidayApiClient;

	@Mock
	private ScheduleRepository scheduleRepository;

	@InjectMocks
	private HolidayScheduleSyncService holidayScheduleSyncService;

	@Test
	@DisplayName("중복 공휴일은 저장하지 않고 신규 공휴일만 저장한다")
	void syncHolidaysSaveOnlyNewHolidays() {
		// given
		HolidayInfo newYear = new HolidayInfo(LocalDate.of(2026, 1, 1), "신정");
		HolidayInfo duplicateInApi = new HolidayInfo(LocalDate.of(2026, 1, 1), "신정");
		HolidayInfo alreadySaved = new HolidayInfo(LocalDate.of(2026, 3, 1), "삼일절");
		HolidayInfo nextYearHoliday = new HolidayInfo(LocalDate.of(2027, 1, 1), "신정");

		given(holidayApiClient.fetchHolidaysByYear(anyInt()))
			.willReturn(List.of(newYear, duplicateInApi, alreadySaved))
			.willReturn(List.of(nextYearHoliday));

		given(scheduleRepository.existsByTypeAndTitleAndStartAndEnd(
			ScheduleType.HOLIDAY,
			newYear.name(),
			newYear.date().atStartOfDay(),
			newYear.date().atTime(LocalTime.MAX))).willReturn(false);

		given(scheduleRepository.existsByTypeAndTitleAndStartAndEnd(
			ScheduleType.HOLIDAY,
			alreadySaved.name(),
			alreadySaved.date().atStartOfDay(),
			alreadySaved.date().atTime(LocalTime.MAX))).willReturn(true);

		given(scheduleRepository.existsByTypeAndTitleAndStartAndEnd(
			ScheduleType.HOLIDAY,
			nextYearHoliday.name(),
			nextYearHoliday.date().atStartOfDay(),
			nextYearHoliday.date().atTime(LocalTime.MAX))).willReturn(false);

		given(scheduleRepository.save(any(Schedule.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		holidayScheduleSyncService.syncHolidays("test-trigger");

		// then
		ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
		then(scheduleRepository).should(times(2)).save(scheduleCaptor.capture());

		List<Schedule> savedSchedules = scheduleCaptor.getAllValues();
		assertThat(savedSchedules)
			.extracting(Schedule::getType)
			.containsOnly(ScheduleType.HOLIDAY);
		assertThat(savedSchedules)
			.extracting(Schedule::getTitle)
			.containsExactlyInAnyOrder("신정", "신정");
		assertThat(savedSchedules)
			.extracting(Schedule::getStart)
			.containsExactlyInAnyOrder(
				LocalDateTime.of(2026, 1, 1, 0, 0),
				LocalDateTime.of(2027, 1, 1, 0, 0));
	}
}

