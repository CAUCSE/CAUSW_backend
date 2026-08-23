package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

@ExtendWith(MockitoExtension.class)
@DisplayName("CauAiNoticeCrawler 테스트")
class CauAiNoticeCrawlerTest {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter LIST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
			given(crawlHttpClient.fetch(any(), any())).willReturn(noticeRows(3491));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).containsExactly(new ArticleUrl(
				"https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=detail&no=3491&keyword=&search=title",
				"3491", "공지"));
		}

		@Test
		@DisplayName("최대 스캔 범위 밖의 공지는 목록 수집에서 제외한다")
		void shouldExcludeNoticesOutsideMaxScanRange_whenListIsFetched() {
			// given
			LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);
			given(crawlHttpClient.fetch(any(), any())).willReturn(noticeRows(
				noticeRow(3491, now.minusDays(3)),
				noticeRow(3490, now.minusDays(4))));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).extracting(ArticleUrl::externalId).containsExactly("3491");
		}

		@Test
		@DisplayName("페이지마다 반복되는 고정 공지를 제외하고 설정된 수만큼 고유 공지를 수집한다")
		void shouldReturnConfiguredNumberOfUniqueNotices_whenPinnedNoticeIsRepeatedAcrossPages() {
			// given
			CrawlContext twoPageContext = new CrawlContext(createCauAiNoticeConfig(3, 2));
			given(crawlHttpClient.fetch("https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=list&currentPage=1",
				twoPageContext.siteConfig())).willReturn(noticeRows(100));
			given(crawlHttpClient.fetch("https://ai.cau.ac.kr/sub07/sub0701.php?category=1&view=list&currentPage=2",
				twoPageContext.siteConfig())).willReturn(noticeRows(100, 101, 102));

			// when
			List<ArticleUrl> result = crawler.fetchList(twoPageContext);

			// then
			assertThat(result).extracting(ArticleUrl::externalId).containsExactly("100", "101", "102");
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

	private SiteConfig createCauAiNoticeConfig(int maxArticles, int maxPages) {
		SiteConfig source = SiteConfigFixture.cauAiNotice();
		return SiteConfig.of(
			source.getSiteId(), source.getTargetBoardId(), CrawlerType.CAU_AI_NOTICE,
			source.getListUrl(), source.getBaseUrl(), Map.of(), Duration.ZERO, Duration.ofSeconds(1),
			1, maxArticles, 3, PaginationType.PAGE_NUMBER, "currentPage", maxPages,
			source.getSelectors(), false, false, true);
	}

	private String noticeRows(int... noticeIds) {
		StringBuilder rows = new StringBuilder("<table class='table-basic'><tbody>");
		for (int noticeId : noticeIds) {
			rows.append(noticeRow(noticeId, LocalDateTime.now(KOREA_ZONE_ID)));
		}
		return rows.append("</tbody></table>").toString();
	}

	private String noticeRows(String... rows) {
		return "<table class='table-basic'><tbody>" + String.join("", rows) + "</tbody></table>";
	}

	private String noticeRow(int noticeId, LocalDateTime announceDate) {
		return """
			<tr><td>공지</td><td class='title'><a href='?category=1&view=detail&no=%d&keyword=&search=title'>공지 %d</a></td>
			<td class='pc-only'>관리자</td><td class='pc-only'>%s</td><td class='pc-only'>1</td></tr>
			"""
			.formatted(noticeId, noticeId, announceDate.format(LIST_DATE_FORMATTER));
	}
}
