package net.causw.app.main.domain.integration.crawled.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;

@DisplayName("SiteConfig 테스트")
class SiteConfigTest {
	@Nested
	@DisplayName("크롤링 실행 허용 시간 판정")
	class IsWithinScheduleTest {
		@Test
		@DisplayName("일반 시간 구간은 시작 시각을 포함하고 종료 시각을 제외한다")
		void isWithinSchedule_shouldIncludeStartAndExcludeEnd() {
			SiteConfig config = create(LocalTime.of(9, 0), LocalTime.of(18, 0));

			assertThat(config.isWithinSchedule(LocalTime.of(9, 0))).isTrue();
			assertThat(config.isWithinSchedule(LocalTime.of(17, 59, 59))).isTrue();
			assertThat(config.isWithinSchedule(LocalTime.of(18, 0))).isFalse();
		}

		@Test
		@DisplayName("자정을 지나는 시간 구간을 판정한다")
		void isWithinSchedule_shouldSupportOvernightRange() {
			SiteConfig config = create(LocalTime.of(22, 0), LocalTime.of(2, 0));

			assertThat(config.isWithinSchedule(LocalTime.of(23, 0))).isTrue();
			assertThat(config.isWithinSchedule(LocalTime.of(1, 59))).isTrue();
			assertThat(config.isWithinSchedule(LocalTime.of(2, 0))).isFalse();
			assertThat(config.isWithinSchedule(LocalTime.NOON)).isFalse();
		}

		@Test
		@DisplayName("시작 시간과 종료 시간이 모두 없으면 항상 실행을 허용한다")
		void isWithinSchedule_shouldAllowAllDay_whenTimesAreNull() {
			SiteConfig config = create(null, null);

			assertThat(config.isWithinSchedule(LocalTime.MIDNIGHT)).isTrue();
			assertThat(config.isWithinSchedule(LocalTime.NOON)).isTrue();
		}

		@Test
		@DisplayName("시작 시간과 종료 시간 중 하나만 없으면 실행을 허용하지 않는다")
		void isWithinSchedule_shouldRejectIncompleteRange() {
			SiteConfig onlyStartTime = create(LocalTime.of(9, 0), null);
			SiteConfig onlyEndTime = create(null, LocalTime.of(18, 0));

			assertThat(onlyStartTime.isWithinSchedule(LocalTime.NOON)).isFalse();
			assertThat(onlyEndTime.isWithinSchedule(LocalTime.NOON)).isFalse();
		}
	}

	private SiteConfig create(LocalTime startTime, LocalTime endTime) {
		return SiteConfig.of(
			"site", "target-board-id", CrawlerType.CAU_SW_NOTICE,
			"https://example.com/list?page=", "https://example.com", Map.of(),
			Duration.ZERO, Duration.ofSeconds(1), 1, 10, 3,
			PaginationType.PAGE_NUMBER, "page", 1, false, false, true,
			startTime, endTime);
	}
}
