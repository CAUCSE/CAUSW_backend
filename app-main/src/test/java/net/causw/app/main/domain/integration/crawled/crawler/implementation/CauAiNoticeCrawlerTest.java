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
@DisplayName("CauAiNoticeCrawler 테스트")
class CauAiNoticeCrawlerTest {
	@InjectMocks
	private CauAiNoticeCrawler crawler;

	@Mock
	private CrawlHttpClient crawlHttpClient;
	private final CrawlContext context = new CrawlContext(SiteConfigFixture.cauAiNotice());

	@Nested
	@DisplayName("목록 수집")
	class FetchList {
		@Test
		@DisplayName("공지 URL의 no를 외부 식별자로 사용한다")
		void shouldUseNoAsExternalId_whenNoticeUrlIsFetched() {
			// given
			given(crawlHttpClient.fetch(any(), any())).willReturn("""
				<table class='table-basic'><tbody>
				<tr><td><span class='tag blue'>공지</span></td>
				<td class='title'><a href='?category=1&view=detail&no=3491&keyword=&search=title'>AI 공지</a></td></tr>
				</tbody></table>
				""");

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).containsExactly(new ArticleUrl(
				"https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=detail&no=3491&keyword=&search=title",
				"3491", "공지"));
		}
	}

	@Nested
	@DisplayName("상세 수집")
	class FetchArticle {
		@Test
		@DisplayName("상세 HTML의 공지 필드와 첨부파일을 파싱한다")
		void shouldParseNoticeFields() {
			// given
			ArticleUrl articleUrl = new ArticleUrl(
				"https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=detail&no=3491", "3491", "공지");
			given(crawlHttpClient.fetch(articleUrl.url(), context.siteConfig())).willReturn("""
				<section class='board'>
				<div class='header'><h3>AI 공지 제목</h3><div><span>2026-08-20 13:27:52</span><span>관리자</span></div></div>
				<div class='fr-view detail'><p>본문</p><img src='/image.png'></div>
				<div class='files'><a href='/board/download.php?id=1'>자료.pdf</a></div>
				</section>
				""");

			// when
			RawArticle result = crawler.fetchArticle(context, articleUrl);

			// then
			assertThat(result.title()).isEqualTo("AI 공지 제목");
			assertThat(result.author()).isEqualTo("관리자");
			assertThat(result.attachments()).containsExactly(
				new net.causw.app.main.domain.integration.crawled.dto.RawAttachment(
					"자료.pdf", "https://ai.cau.ac.kr/board/download.php?id=1"));
		}
	}
}
