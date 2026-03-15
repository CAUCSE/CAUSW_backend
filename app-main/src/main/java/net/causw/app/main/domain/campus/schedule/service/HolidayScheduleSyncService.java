package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.repository.ScheduleRepository;
import net.causw.app.main.domain.campus.schedule.service.HolidayApiClient.HolidayInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayScheduleSyncService {

	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

	private final HolidayApiClient holidayApiClient;
	private final ScheduleRepository scheduleRepository;

	@EventListener(ApplicationReadyEvent.class)
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

		Set<String> duplicateFilter = new HashSet<>();
		int savedCount = 0;
		int skippedCount = 0;
		for (HolidayInfo holiday : holidays) {
			if (!duplicateFilter.add(uniqueKey(holiday))) {
				skippedCount++;
				continue;
			}

			LocalDateTime start = holiday.date().atStartOfDay();
			LocalDateTime end = holiday.date().atTime(LocalTime.MAX);
			boolean exists = scheduleRepository.existsByTypeAndTitleAndStartAndEnd(
				ScheduleType.HOLIDAY,
				holiday.name(),
				start,
				end);

			if (exists) {
				skippedCount++;
				continue;
			}

			scheduleRepository.save(Schedule.of(
				holiday.name(),
				ScheduleType.HOLIDAY,
				start,
				end,
				null,
				null));
			savedCount++;
		}

		log.info("공휴일 동기화를 완료했습니다. trigger={}, 대상년도={}-{}, 조회={}, 저장={}, 스킵={}",
			trigger,
			currentYear,
			currentYear + 1,
			holidays.size(),
			savedCount,
			skippedCount);
	}

	private List<HolidayInfo> collectHolidays(int currentYear) {
		List<HolidayInfo> currentYearHolidays = holidayApiClient.fetchHolidaysByYear(currentYear);
		List<HolidayInfo> nextYearHolidays = holidayApiClient.fetchHolidaysByYear(currentYear + 1);
		return java.util.stream.Stream.concat(currentYearHolidays.stream(), nextYearHolidays.stream())
			.toList();
	}

	private String uniqueKey(HolidayInfo holiday) {
		return holiday.date() + ":" + holiday.name();
	}
}


