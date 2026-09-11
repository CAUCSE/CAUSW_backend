package net.causw.app.main.domain.integration.crawled;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public final class SiteConfigFixture {
	private SiteConfigFixture() {}

	public static SiteConfig create() {
		return create("site");
	}

	public static SiteConfig create(String siteId) {
		return create(siteId, null, null);
	}

	public static SiteConfig create(String siteId, LocalTime startTime, LocalTime endTime) {
		return create(siteId, CrawlerType.CAU_SW_NOTICE, startTime, endTime);
	}

	public static SiteConfig create(
		String siteId,
		CrawlerType crawlerType,
		LocalTime startTime,
		LocalTime endTime) {
		return SiteConfig.of(
			siteId,
			"target-board-id",
			crawlerType,
			"https://example.com/list?page=",
			"https://example.com",
			Map.of(),
			Duration.ZERO,
			Duration.ofSeconds(1),
			1,
			10,
			3,
			PaginationType.PAGE_NUMBER,
			"page",
			1,
			false,
			false,
			true,
			startTime,
			endTime);
	}

	public static SiteConfig cauSwNotice() {
		return SiteConfig.of(
			"cau-sw-notice",
			"target-board-id",
			CrawlerType.CAU_SW_NOTICE,
			"https://cse.cau.ac.kr/sub05/sub0501.php?offset=",
			"https://cse.cau.ac.kr",
			Map.of(),
			Duration.ZERO,
			Duration.ofSeconds(1),
			1,
			30,
			3,
			PaginationType.PAGE_NUMBER,
			"offset",
			1,
			false,
			false,
			true);
	}

	public static SiteConfig cauAiNotice() {
		return SiteConfig.of(
			"cau-ai-notice",
			"target-board-id",
			CrawlerType.CAU_AI_NOTICE,
			"https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=list&currentPage=",
			"https://ai.cau.ac.kr",
			Map.of(),
			Duration.ZERO,
			Duration.ofSeconds(1),
			1,
			30,
			3,
			PaginationType.PAGE_NUMBER,
			"currentPage",
			1,
			false,
			false,
			true);
	}
}
