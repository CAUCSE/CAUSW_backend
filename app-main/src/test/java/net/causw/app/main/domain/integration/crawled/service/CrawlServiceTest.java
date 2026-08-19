package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.core.SiteCrawlerRegistry;
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.implementation.SiteConfigReader;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlService 테스트")
class CrawlServiceTest {
	@InjectMocks
	private CrawlService crawlService;

	@Mock
	private SiteCrawlerRegistry registry;
	@Mock
	private SiteConfigReader siteConfigReader;
	@Mock
	private CrawledArticleCleaner cleaner;
	@Mock
	private CrawledNoticeUpsertService crawledNoticeUpsertService;
	@Mock
	private SiteCrawler crawler;

	@Test
	@DisplayName("개별 공지 실패 후에도 다음 공지를 계속 처리한다")
	void crawl_shouldContinue_whenOneArticleFails() {
		// given
		ArticleUrl failed = new ArticleUrl("https://example.com/1", "1", "공지");
		ArticleUrl succeeded = new ArticleUrl("https://example.com/2", "2", "공지");
		SiteConfig config = config();
		CrawlContext context = new CrawlContext(config);
		RawArticle raw = new RawArticle("site", "2", succeeded.url(), "공지", "제목", "본문", "관리자",
			"2026-08-10", null, List.of());
		CleanArticle clean = new CleanArticle("site", "target-board-id", "2", succeeded.url(), "공지", "제목", "본문", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "hash");

		given(siteConfigReader.getEnabledBySiteId("site")).willReturn(config);
		given(registry.get(config.getCrawlerType())).willReturn(crawler);
		given(crawler.fetchList(context)).willReturn(List.of(failed, succeeded));
		given(crawler.fetchArticle(context, failed)).willThrow(new IllegalStateException("failed"));
		given(crawler.fetchArticle(context, succeeded)).willReturn(raw);
		given(cleaner.clean(raw, config)).willReturn(clean);
		given(crawledNoticeUpsertService.upsert(clean)).willReturn(CrawlSaveStatus.CREATED);

		// when
		CrawlResult result = crawlService.crawl("site");

		// then
		assertThat(result.createdCount()).isEqualTo(1);
		assertThat(result.failedUrls()).containsExactly(failed.url());
	}

	@Test
	@DisplayName("한 사이트가 실패해도 다음 활성 사이트를 계속 수집한다")
	void crawlAllEnabled_shouldContinue_whenOneSiteFails() {
		// given
		SiteConfig failedConfig = SiteConfigFixture.create("failed-site");
		SiteConfig succeededConfig = SiteConfigFixture.create("succeeded-site");
		CrawlContext failedContext = new CrawlContext(failedConfig);
		CrawlContext succeededContext = new CrawlContext(succeededConfig);

		given(siteConfigReader.findAllEnabled()).willReturn(List.of(failedConfig, succeededConfig));
		given(registry.get(failedConfig.getCrawlerType())).willReturn(crawler);
		given(crawler.fetchList(failedContext)).willThrow(new IllegalStateException("failed"));
		given(crawler.fetchList(succeededContext)).willReturn(List.of());

		// when
		List<CrawlResult> results = crawlService.crawlAllEnabled();

		// then
		assertThat(results).singleElement()
			.extracting(CrawlResult::siteId)
			.isEqualTo("succeeded-site");
	}

	private SiteConfig config() {
		return SiteConfigFixture.create();
	}
}
