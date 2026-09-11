package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
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
@DisplayName("NoticeCrawlingFacade 테스트")
class NoticeCrawlingFacadeTest {
	@InjectMocks
	private NoticeCrawlingFacade noticeCrawlingFacade;

	@Mock
	private SiteCrawlerRegistry registry;
	@Mock
	private SiteConfigReader siteConfigReader;
	@Mock
	private CrawledArticleCleaner cleaner;
	@Mock
	private CrawledNoticePersistenceService crawledNoticePersistenceService;
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
		given(crawledNoticePersistenceService.persistAll(config, List.of(clean)))
			.willReturn(Map.of("2", CrawlSaveStatus.CREATED));

		// when
		CrawlResult result = noticeCrawlingFacade.crawl("site");

		// then
		assertThat(result.createdCount()).isEqualTo(1);
		assertThat(result.skippedCount()).isZero();
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
		given(crawledNoticePersistenceService.persistAll(succeededConfig, List.of())).willReturn(Map.of());

		// when
		List<CrawlResult> results = noticeCrawlingFacade.crawlAllEnabled();

		// then
		assertThat(results).singleElement()
			.extracting(CrawlResult::siteId)
			.isEqualTo("succeeded-site");
	}

	@Test
	@DisplayName("현재 시각이 실행 허용 구간인 활성 사이트만 수집한다")
	void crawlAllEnabled_shouldCrawlOnlySitesWithinSchedule() {
		// given
		SiteConfig allowedConfig = SiteConfigFixture.create(
			"allowed-site", LocalTime.of(9, 0), LocalTime.of(18, 0));
		SiteConfig blockedConfig = SiteConfigFixture.create(
			"blocked-site", CrawlerType.CAU_AI_NOTICE, LocalTime.of(18, 0), LocalTime.of(9, 0));
		CrawlContext allowedContext = new CrawlContext(allowedConfig);

		given(siteConfigReader.findAllEnabled()).willReturn(List.of(allowedConfig, blockedConfig));
		given(registry.get(allowedConfig.getCrawlerType())).willReturn(crawler);
		given(crawler.fetchList(allowedContext)).willReturn(List.of());
		given(crawledNoticePersistenceService.persistAll(allowedConfig, List.of())).willReturn(Map.of());

		// when
		List<CrawlResult> results = noticeCrawlingFacade.crawlAllEnabled(LocalTime.NOON);

		// then
		assertThat(results)
			.extracting(CrawlResult::siteId)
			.containsExactly("allowed-site");
		then(registry).should(never()).get(CrawlerType.CAU_AI_NOTICE);
	}

	@Test
	@DisplayName("설정된 크롤링 시간대를 기준으로 현재 실행 시각을 계산한다")
	void crawlAllEnabled_shouldUseConfiguredZone() {
		// given
		ReflectionTestUtils.setField(noticeCrawlingFacade, "crawlZone", "UTC");
		LocalTime utcNow = LocalTime.now(ZoneId.of("UTC"));
		SiteConfig config = SiteConfigFixture.create(
			"utc-site", utcNow.minusHours(1), utcNow.plusHours(1));
		CrawlContext context = new CrawlContext(config);

		given(siteConfigReader.findAllEnabled()).willReturn(List.of(config));
		given(registry.get(config.getCrawlerType())).willReturn(crawler);
		given(crawler.fetchList(context)).willReturn(List.of());
		given(crawledNoticePersistenceService.persistAll(config, List.of())).willReturn(Map.of());

		// when
		List<CrawlResult> results = noticeCrawlingFacade.crawlAllEnabled();

		// then
		assertThat(results)
			.extracting(CrawlResult::siteId)
			.containsExactly("utc-site");
	}

	private SiteConfig config() {
		return SiteConfigFixture.create();
	}
}
