package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("CauNoticeCrawler 테스트")
class CauNoticeCrawlerTest {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

	@Mock
	private CrawlHttpClient crawlHttpClient;

	@Nested
	@DisplayName("목록 수집")
	class FetchList {
		@Test
		@DisplayName("JSON 목록 응답의 공지 URL과 등록일을 수집한다")
		void shouldCollectArticleUrls_whenJsonListIsReturned() {
			// given
			CrawlContext context = new CrawlContext(siteConfig());
			CauNoticeCrawler crawler = new CauNoticeCrawler(crawlHttpClient, new ObjectMapper());
			String announcedAt = LocalDate.now().format(DATE_FORMATTER);
			given(crawlHttpClient.fetch(any(), any())).willReturn("""
				{"data":{"list":[{"WRITE_DATE":"%s","BBS_SEQ":"123","CATEGORY_NM":"학사"}]}}
				""".formatted(announcedAt));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).containsExactly(new ArticleUrl(
				"https://www.cau.ac.kr/cms/FR_CON/BoardView.do?MENU_ID=100&CONTENTS_NO=2&SITE_NO=2&P_TAB_NO=2&TAB_NO=&BOARD_SEQ=4&BOARD_CATEGORY_NO=&BBS_SEQ=123&pageNo=1",
				"123", "학사", announcedAt));
		}

		@Test
		@DisplayName("HTML 목록 응답의 공지 URL과 등록일을 수집한다")
		void shouldCollectArticleUrls_whenHtmlListIsReturned() {
			// given
			CrawlContext context = new CrawlContext(siteConfig());
			CauNoticeCrawler crawler = new CauNoticeCrawler(crawlHttpClient, new ObjectMapper());
			String announcedAt = LocalDate.now().format(DATE_FORMATTER);
			given(crawlHttpClient.fetch(any(), any())).willReturn(
				"""
					<div id='contents'><table><tbody>
					<tr><td>학사</td><td><a href='/cms/FR_CON/BoardView.do?MENU_ID=100&CONTENTS_NO=2&SITE_NO=2&BOARD_SEQ=4&BBS_SEQ=456&pageNo=1'>수강신청 안내</a></td><td>관리자</td><td>%s</td><td>1</td></tr>
					</tbody></table></div>
					"""
					.formatted(announcedAt));

			// when
			List<ArticleUrl> result = crawler.fetchList(context);

			// then
			assertThat(result).containsExactly(new ArticleUrl(
				"https://www.cau.ac.kr/cms/FR_CON/BoardView.do?MENU_ID=100&CONTENTS_NO=2&SITE_NO=2&BOARD_SEQ=4&BBS_SEQ=456&pageNo=1",
				"456", "학사", announcedAt));
		}
	}

	private SiteConfig siteConfig() {
		return SiteConfig.of(
			"cau-academic-notice",
			"target-board-id",
			CrawlerType.CAU_NOTICE,
			"https://www.cau.ac.kr/ajax/FR_SVC/BoardList.do?MENU_ID=100&CONTENTS_NO=2&P_TAB_NO=2&BOARD_CATEGORY_NO=&pageNo=",
			"https://www.cau.ac.kr",
			Map.of(),
			Duration.ZERO,
			Duration.ofSeconds(1),
			1,
			10,
			3,
			PaginationType.PAGE_NUMBER,
			"pageNo",
			1,
			"yyyy.MM.dd",
			false,
			false,
			true);
	}
}
