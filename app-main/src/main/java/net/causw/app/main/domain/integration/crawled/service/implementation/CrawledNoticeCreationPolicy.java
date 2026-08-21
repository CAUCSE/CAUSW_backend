package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

/**
 * 새 크롤링 공지의 저장 가능 기간을 판단합니다.
 */
@Component
public class CrawledNoticeCreationPolicy {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

	/**
	 * 공지일이 한국 시간 기준 오늘 또는 전날인지 확인합니다.
	 *
	 * <p>크롤링 원본의 공지 시각은 일 단위로만 제공되므로, 24시간 정책은 해당 일자 범위로 적용합니다.</p>
	 *
	 * @param announceDate 저장 여부를 판단할 공지일
	 * @return 저장 가능 기간에 포함되면 {@code true}
	 */
	public boolean isWithinCreationWindow(LocalDate announceDate) {
		LocalDate today = LocalDate.now(KOREA_ZONE_ID);
		LocalDate oldestAllowedDate = today.minusDays(1);
		return !announceDate.isBefore(oldestAllowedDate) && !announceDate.isAfter(today);
	}
}
