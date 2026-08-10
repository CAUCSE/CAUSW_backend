package net.causw.app.main.domain.integration.crawled.core;

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
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.CrawledArticleCleaner;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.SiteConfigReader;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlPipeline 테스트")
class CrawlPipelineTest {
	@InjectMocks
	private CrawlPipeline pipeline;

	@Mock
	private SiteCrawlerRegistry registry;
	@Mock
	private SiteConfigReader siteConfigReader;
	@Mock
	private CrawledArticleCleaner cleaner;
	@Mock
	private CrawledNoticeWriter writer;
	@Mock
	private SiteCrawler crawler;

	@Test
	@DisplayName("개별 공지 실패 후에도 다음 공지를 계속 처리한다")
	void run_shouldContinue_whenOneArticleFails() {
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
		given(writer.upsert(clean)).willReturn(CrawlSaveStatus.CREATED);

		// when
		CrawlResult result = pipeline.run("site");

		// then
		assertThat(result.createdCount()).isEqualTo(1);
		assertThat(result.failedUrls()).containsExactly(failed.url());
	}

	private SiteConfig config() {
		return SiteConfigFixture.create();
	}
}
