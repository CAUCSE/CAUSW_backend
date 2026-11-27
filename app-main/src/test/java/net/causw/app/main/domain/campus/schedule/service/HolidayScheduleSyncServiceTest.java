package net.causw.app.main.domain.campus.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.HolidayApiClient.HolidayInfo;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;

@ExtendWith(MockitoExtension.class)
class HolidayScheduleSyncServiceTest {

	@Mock
	private HolidayApiClient holidayApiClient;

	@Mock
	private ScheduleReader scheduleReader;

	@Mock
	private ScheduleWriter scheduleWriter;

	@InjectMocks
	private HolidayScheduleSyncService holidayScheduleSyncService;

	private int currentYear;

	@BeforeEach
	void setUp() {
		currentYear = Year.now(ZoneId.of("Asia/Seoul")).getValue();
	}

	@Test
	@DisplayName("기존 공휴일을 한 번에 조회하고, 기존/API 내 중복 공휴일을 제외한 신규 공휴일만 저장한다")
	void syncHolidaysSaveOnlyNewHolidays() {
		// given
		HolidayInfo newYear = new HolidayInfo(LocalDate.of(currentYear, 1, 1), "신정");
		HolidayInfo duplicateInApi = new HolidayInfo(LocalDate.of(currentYear, 1, 1), "신정");
		HolidayInfo alreadySaved = new HolidayInfo(LocalDate.of(currentYear, 3, 1), "삼일절");
		HolidayInfo nextYearHoliday = new HolidayInfo(LocalDate.of(currentYear + 1, 1, 1), "신정");

		given(holidayApiClient.fetchHolidaysByYear(anyInt()))
			.willReturn(List.of(newYear, duplicateInApi, alreadySaved))
			.willReturn(List.of(nextYearHoliday));
		given(scheduleReader.findByCondition(any(), any(), any()))
			.willReturn(List.of(holidaySchedule(alreadySaved)));

		// when
		holidayScheduleSyncService.syncHolidays("test-trigger");

		// then
		then(scheduleReader).should(times(1)).findByCondition(
			LocalDateTime.of(currentYear, 1, 1, 0, 0),
			LocalDateTime.of(currentYear + 1, 12, 31, 23, 59, 59),
			List.of(ScheduleType.HOLIDAY));

		List<Schedule> savedSchedules = captureSavedSchedules();
		assertThat(savedSchedules)
			.extracting(Schedule::getType)
			.containsOnly(ScheduleType.HOLIDAY);
		assertThat(savedSchedules)
			.extracting(Schedule::getTitle, Schedule::getStart, Schedule::getEnd)
			.containsExactlyInAnyOrder(
				tuple("신정",
					LocalDateTime.of(currentYear, 1, 1, 0, 0),
					LocalDateTime.of(currentYear, 1, 1, 23, 59, 59)),
				tuple("신정",
					LocalDateTime.of(currentYear + 1, 1, 1, 0, 0),
					LocalDateTime.of(currentYear + 1, 1, 1, 23, 59, 59)));
	}

	@Test
	@DisplayName("제목이 같아도 시작/종료 시각이 다른 기존 일정은 중복으로 보지 않는다")
	void syncHolidaysTreatsDifferentTimeRangeAsNew() {
		// given
		HolidayInfo holiday = new HolidayInfo(LocalDate.of(currentYear, 5, 5), "어린이날");
		Schedule manuallyEdited = Schedule.of("어린이날", ScheduleType.HOLIDAY,
			LocalDateTime.of(currentYear, 5, 5, 9, 0),
			LocalDateTime.of(currentYear, 5, 5, 18, 0),
			null, null);

		given(holidayApiClient.fetchHolidaysByYear(anyInt()))
			.willReturn(List.of(holiday))
			.willReturn(List.of());
		given(scheduleReader.findByCondition(any(), any(), any())).willReturn(List.of(manuallyEdited));

		// when
		holidayScheduleSyncService.syncHolidays("test-trigger");

		// then
		assertThat(captureSavedSchedules())
			.extracting(Schedule::getTitle, Schedule::getStart)
			.containsExactly(tuple("어린이날",
				LocalDateTime.of(currentYear, 5, 5, 0, 0)));
	}

	@Test
	@DisplayName("모든 공휴일이 이미 저장되어 있으면 저장하지 않는다")
	void syncHolidaysSkipSaveWhenAllExist() {
		// given
		HolidayInfo newYear = new HolidayInfo(LocalDate.of(currentYear, 1, 1), "신정");
		HolidayInfo nextYearHoliday = new HolidayInfo(LocalDate.of(currentYear + 1, 1, 1), "신정");

		given(holidayApiClient.fetchHolidaysByYear(anyInt()))
			.willReturn(List.of(newYear))
			.willReturn(List.of(nextYearHoliday));
		given(scheduleReader.findByCondition(any(), any(), any()))
			.willReturn(List.of(holidaySchedule(newYear), holidaySchedule(nextYearHoliday)));

		// when
		holidayScheduleSyncService.syncHolidays("test-trigger");

		// then
		then(scheduleWriter).should(never()).saveAll(anyList());
	}

	@Test
	@DisplayName("공휴일 API 조회에 실패하면 기존 일정 조회와 저장을 하지 않는다")
	void syncHolidaysDoNothingWhenApiFails() {
		// given
		given(holidayApiClient.fetchHolidaysByYear(anyInt())).willThrow(new RuntimeException("api error"));

		// when
		holidayScheduleSyncService.syncHolidays("test-trigger");

		// then
		then(scheduleReader).shouldHaveNoInteractions();
		then(scheduleWriter).shouldHaveNoInteractions();
	}

	@SuppressWarnings("unchecked")
	private List<Schedule> captureSavedSchedules() {
		ArgumentCaptor<List<Schedule>> captor = ArgumentCaptor.forClass(List.class);
		then(scheduleWriter).should(times(1)).saveAll(captor.capture());
		return captor.getValue();
	}

	private Schedule holidaySchedule(HolidayInfo holiday) {
		return Schedule.of(
			holiday.name(),
			ScheduleType.HOLIDAY,
			holiday.date().atStartOfDay(),
			holiday.date().atTime(23, 59, 59),
			null,
			null);
	}
}
