package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.HolidayApiClient.HolidayInfo;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleReader;
import net.causw.app.main.domain.campus.schedule.service.implementation.ScheduleWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayScheduleSyncService {

	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

	private final HolidayApiClient holidayApiClient;
	private final ScheduleReader scheduleReader;
	private final ScheduleWriter scheduleWriter;

	@EventListener(ApplicationReadyEvent.class)
	@Order(1)
	@Transactional
	public void syncOnApplicationReady() {
		syncHolidays("application-ready");
	}

	@Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
	@Transactional
	public void syncMonthly() {
		syncHolidays("monthly-cron");
	}

	public void syncHolidays(String trigger) {
		int currentYear = Year.now(KOREA_ZONE_ID).getValue();
		List<HolidayInfo> holidays;
		try {
			holidays = collectHolidays(currentYear);
		} catch (Exception e) {
			log.error("공휴일 조회에 실패했습니다. trigger={}", trigger, e);
			return;
		}

		// 동기화 대상 기간의 기존 공휴일을 한 번에 조회하고, API 응답 내 중복 확인에도 같은 Set을 사용
		Set<HolidayKey> knownKeys = findExistingHolidayKeys(currentYear);
		List<Schedule> newSchedules = new ArrayList<>();
		for (HolidayInfo holiday : holidays) {
			HolidayKey key = HolidayKey.from(holiday);
			if (knownKeys.add(key)) {
				newSchedules.add(Schedule.of(key.title(), ScheduleType.HOLIDAY, key.start(), key.end(), null, null));
			}
		}

		if (!newSchedules.isEmpty()) {
			scheduleWriter.saveAll(newSchedules);
		}

		log.info("공휴일 동기화를 완료했습니다. trigger={}, 대상년도={}-{}, 조회={}, 저장={}, 스킵={}",
			trigger,
			currentYear,
			currentYear + 1,
			holidays.size(),
			newSchedules.size(),
			holidays.size() - newSchedules.size());
	}

	private List<HolidayInfo> collectHolidays(int currentYear) {
		List<HolidayInfo> currentYearHolidays = holidayApiClient.fetchHolidaysByYear(currentYear);
		List<HolidayInfo> nextYearHolidays = holidayApiClient.fetchHolidaysByYear(currentYear + 1);
		return Stream.concat(currentYearHolidays.stream(), nextYearHolidays.stream())
			.toList();
	}

	private Set<HolidayKey> findExistingHolidayKeys(int currentYear) {
		LocalDateTime from = LocalDate.of(currentYear, 1, 1).atStartOfDay();
		LocalDateTime to = LocalDate.of(currentYear + 1, 12, 31).atTime(23, 59, 59);
		return scheduleReader.findByCondition(from, to, List.of(ScheduleType.HOLIDAY))
			.stream()
			.map(HolidayKey::from)
			.collect(Collectors.toCollection(HashSet::new));
	}

	record HolidayKey(String title, LocalDateTime start, LocalDateTime end) {

		static HolidayKey from(HolidayInfo holiday) {
			return new HolidayKey(
				holiday.name(),
				holiday.date().atStartOfDay(),
				holiday.date().atTime(23, 59, 59));
		}

		static HolidayKey from(Schedule schedule) {
			return new HolidayKey(schedule.getTitle(), schedule.getStart(), schedule.getEnd());
		}
	}
}
