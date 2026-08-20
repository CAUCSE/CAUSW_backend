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
}
