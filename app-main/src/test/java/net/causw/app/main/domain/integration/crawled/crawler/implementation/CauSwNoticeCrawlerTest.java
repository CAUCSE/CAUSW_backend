package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;

@ExtendWith(MockitoExtension.class)
@DisplayName("CauSwNoticeCrawler 테스트")
class CauSwNoticeCrawlerTest {
	@InjectMocks
	private CauSwNoticeCrawler crawler;

	@Mock
	private CrawlHttpClient crawlHttpClient;
	private final CrawlContext context = new CrawlContext(SiteConfigFixture.cauSwNotice());

	@Nested
	@DisplayName("목록 수집")
	class FetchList {
		@Test
		@DisplayName("빈 목록은 빈 결과로 반환한다")
		void shouldReturnEmptyList_whenListIsEmpty() {
			// given
			given(crawlHttpClient.fetch(any(), any())).willReturn("<table><tbody></tbody></table>");

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("상세 수집")
	class FetchArticle {
		@Test
		@DisplayName("상세 HTML을 요청하고 공지 필드와 첨부파일을 파싱한다")
		void shouldParseNoticeFields() {
			// given
			ArticleUrl articleUrl = new ArticleUrl(
				"https://cse.cau.ac.kr/notice?uid=10", "https://cse.cau.ac.kr/notice?uid=10", "NEW 공지");
			String html = """
				<div class='header'><h3>제목</h3><div>
				<span>2026-08-10</span><span>라벨</span><span>관리자</span><span>라벨2</span>
				</div></div>
				<div class='fr-view'><p>본문</p><a href='/download.php?id=1'>자료.pdf</a></div>
				""";
			given(crawlHttpClient.fetch(articleUrl.url(), context.siteConfig())).willReturn(html);

			// when
			RawArticle result = crawler.fetchArticle(context, articleUrl);

			// then
			assertThat(result.title()).isEqualTo("제목");
			assertThat(result.author()).isEqualTo("관리자");
			assertThat(result.category()).isEqualTo("공지");
			assertThat(result.attachments()).hasSize(1);
		}
	}
}
