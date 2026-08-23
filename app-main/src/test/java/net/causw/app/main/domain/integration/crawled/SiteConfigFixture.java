package net.causw.app.main.domain.integration.crawled;

import java.time.Duration;
import java.util.Map;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.entity.SiteSelectors;

public final class SiteConfigFixture {
	private SiteConfigFixture() {}

	public static SiteConfig create() {
		return create("site");
	}

	public static SiteConfig create(String siteId) {
		return SiteConfig.of(
			siteId,
			"target-board-id",
			CrawlerType.CAU_SW_NOTICE,
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
			SiteSelectors.of("tr", "a", ".type", "h1", ".body", ".date", ".author", "img",
				"yyyy-MM-dd", null),
			false,
			false,
			true);
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
			SiteSelectors.of(
				"table.table-basic tbody tr",
				"td.aleft a",
				"td span.tag",
				"section#content div.header > h3",
				"section#content div.fr-view",
				"div.header > div > span:nth-of-type(1)",
				"section#content div.header > div > span:nth-of-type(3)",
				"section#content div.fr-view > p > img",
				"yyyy-MM-dd",
				null),
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
			SiteSelectors.of(
				"table.table-basic tbody tr",
				"td.title a",
				"td:first-child",
				"section.board div.header > h3",
				"section.board div.fr-view.detail",
				"section.board div.header > div > span:nth-of-type(1)",
				"section.board div.header > div > span:nth-of-type(2)",
				"section.board div.fr-view.detail img",
				"yyyy-MM-dd HH:mm:ss",
				null),
			false,
			false,
			true);
	}
}
