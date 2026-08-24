package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.LocalDate;
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

import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawAttachment;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

@ExtendWith(MockitoExtension.class)
@DisplayName("CauSwEduNoticeCrawler 테스트")
class CauSwEduNoticeCrawlerTest {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@InjectMocks
	private CauSwEduNoticeCrawler crawler;

	@Mock
	private CrawlHttpClient crawlHttpClient;

	private final CrawlContext context = new CrawlContext(siteConfig());

	@Nested
	@DisplayName("목록 수집")
	class FetchList {
		@Test
		@DisplayName("공지 URL의 boardid와 목록 등록일을 함께 수집한다")
		void shouldUseBoardIdAndListDate_whenNoticeUrlIsFetched() {
			// given
			LocalDate today = LocalDate.now(KOREA_ZONE_ID);
			given(crawlHttpClient.fetch(any(), any())).willReturn(noticeRows(noticeRow(821, today)));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).containsExactly(new ArticleUrl(
				"https://swedu.cau.ac.kr/board/view?menuid=001005005&pagesize=10&boardtypeid=7&boardid=821",
				"821", "공지", today.format(DATE_FORMATTER)));
		}

		@Test
		@DisplayName("최대 스캔 범위 밖의 공지는 목록 수집에서 제외한다")
		void shouldExcludeNoticesOutsideMaxScanRange_whenListIsFetched() {
			// given
			LocalDate today = LocalDate.now(KOREA_ZONE_ID);
			given(crawlHttpClient.fetch(any(), any())).willReturn(noticeRows(
				noticeRow(821, today.minusDays(3)),
				noticeRow(820, today.minusDays(4))));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).extracting(ArticleUrl::externalId).containsExactly("821");
		}
	}

	@Nested
	@DisplayName("상세 수집")
	class FetchArticle {
		@Test
		@DisplayName("목록 등록일과 상세 HTML의 공지 필드 및 첨부파일을 파싱한다")
		void shouldParseNoticeFieldsUsingListDate() {
			// given
			ArticleUrl articleUrl = new ArticleUrl(
				"https://swedu.cau.ac.kr/board/view?menuid=001005005&pagesize=10&boardtypeid=7&boardid=821",
				"821", "공지", "2026-08-10");
			given(crawlHttpClient.fetch(articleUrl.url(), context.siteConfig())).willReturn(
				"""
					<div class='list_type_h1 web_view'>
					<table>
					<thead><tr><th><div class='tit'><p class='l'>SW교육원 공지 제목</p></div></th></tr></thead>
					<tbody>
					<tr><th>첨부파일</th><td><a href='/common/file/fileDownload?filename=notice.pdf'>자료.pdf</a></td></tr>
					<tr><td class='editTd'><p>본문</p><img src='/image.png'></td></tr>
					</tbody>
					</table>
					</div>
					""");

			// when
			RawArticle result = crawler.fetchArticle(context, articleUrl);

			// then
			assertThat(result.title()).isEqualTo("SW교육원 공지 제목");
			assertThat(result.author()).isEqualTo("SW교육원");
			assertThat(result.announcedAt()).isEqualTo("2026-08-10");
			assertThat(result.attachments()).containsExactly(new RawAttachment(
				"자료.pdf", "https://swedu.cau.ac.kr/common/file/fileDownload?filename=notice.pdf"));
		}
	}

	private SiteConfig siteConfig() {
		return SiteConfig.of(
			"cau-sw-edu-notice",
			"target-board-id",
			CrawlerType.CAU_SW_EDU_NOTICE,
			"https://swedu.cau.ac.kr/board/list?boardtypeid=7&menuid=001005005&pagesize=10&currentpage=",
			"https://swedu.cau.ac.kr",
			Map.of(),
			Duration.ZERO,
			Duration.ofSeconds(1),
			1,
			10,
			3,
			PaginationType.PAGE_NUMBER,
			"currentpage",
			1,
			"yyyy-MM-dd",
			false,
			false,
			true);
	}

	private String noticeRows(String... rows) {
		return "<div class='list_type_h1 web_view'><table><tbody>" + String.join("", rows)
			+ "</tbody></table></div>";
	}

	private String noticeRow(int boardId, LocalDate announcedAt) {
		return """
			<tr>
			<td>공지</td>
			<td class='tl'><a href='/board/view?menuid=001005005&pagesize=10&boardtypeid=7&boardid=%d'>공지 %d</a></td>
			<td>%s</td>
			<td>1</td>
			</tr>
			""".formatted(boardId, boardId, announcedAt.format(DATE_FORMATTER));
	}
}
