package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawAttachment;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 중앙대학교 SW교육원 공지를 수집합니다.
 *
 * <p>상세 페이지에 등록일이 없어 목록에서 추출한 등록일을 수집 대상과 함께 전달합니다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CauSwEduNoticeCrawler implements SiteCrawler {
	// 목록 날짜 판별에 사용할 시간대와 형식
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter LIST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// 공지 목록 파싱용 CSS 셀렉터
	private static final String ARTICLE_ROW_SELECTOR = ".list_type_h1.web_view tbody tr";
	private static final String ARTICLE_LINK_SELECTOR = "td.tl a";
	private static final String ARTICLE_CATEGORY_SELECTOR = "td:first-child";
	private static final String LIST_DATE_SELECTOR = "td:nth-last-child(2)";

	// 공지 상세 파싱용 CSS 셀렉터와 고정 작성자
	private static final String TITLE_SELECTOR = ".list_type_h1.web_view thead .tit .l";
	private static final String BODY_SELECTOR = ".list_type_h1.web_view tbody td.editTd";
	private static final String REPRESENTATIVE_IMAGE_SELECTOR = ".list_type_h1.web_view tbody td.editTd img";
	private static final String AUTHOR = "SW교육원";

	// 첨부파일 파싱용 CSS 셀렉터
	private static final String ATTACHMENT_SELECTOR = ".list_type_h1.web_view tbody th:matchesOwn(첨부파일) + td a[href]";

	// 공지 URL에서 외부 식별자를 추출하기 위한 패턴
	private static final Pattern BOARD_ID_PATTERN = Pattern.compile("[?&]boardid=(\\d+)");

	private final CrawlHttpClient crawlHttpClient;

	@Override
	public CrawlerType getCrawlerType() {
		return CrawlerType.CAU_SW_EDU_NOTICE;
	}

	@Override
	public List<ArticleUrl> fetchList(CrawlContext context) {
		SiteConfig siteConfig = context.siteConfig();
		Map<String, ArticleUrl> articleUrlsByExternalId = new LinkedHashMap<>();

		for (int page = 1; page <= siteConfig.getMaxPages()
			&& articleUrlsByExternalId.size() < siteConfig.getMaxArticles(); page++) {
			String listUrl = siteConfig.getListUrl() + page;
			Document document = Jsoup.parse(crawlHttpClient.fetch(listUrl, siteConfig), listUrl);
			Elements rows = document.select(ARTICLE_ROW_SELECTOR);
			if (rows.isEmpty()) {
				break;
			}

			for (Element row : rows) {
				if (articleUrlsByExternalId.size() >= siteConfig.getMaxArticles()) {
					break;
				}
				// 상세 페이지에 등록일이 없으므로 목록에서 얻은 날짜를 수집 대상에 전달합니다.
				String announcedAt = requiredText(row, LIST_DATE_SELECTOR);
				if (!isWithinScanRange(announcedAt, siteConfig)) {
					continue;
				}
				Element linkElement = row.selectFirst(ARTICLE_LINK_SELECTOR);
				if (linkElement == null || linkElement.absUrl("href").isBlank()) {
					continue;
				}
				String url = linkElement.absUrl("href");
				String externalId = extractBoardId(url);
				articleUrlsByExternalId.putIfAbsent(externalId,
					new ArticleUrl(url, externalId, row.select(ARTICLE_CATEGORY_SELECTOR).text(), announcedAt));
			}
		}

		return List.copyOf(articleUrlsByExternalId.values());
	}

	private boolean isWithinScanRange(String announcedAt, SiteConfig siteConfig) {
		try {
			LocalDate announceDate = LocalDate.parse(announcedAt.trim(), LIST_DATE_FORMATTER);
			LocalDate today = LocalDate.now(KOREA_ZONE_ID);
			return !announceDate.isBefore(today.minusDays(siteConfig.getMaxScanRangeDays()))
				&& !announceDate.isAfter(today);
		} catch (DateTimeParseException e) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	@Override
	public RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl) {
		SiteConfig siteConfig = context.siteConfig();
		try {
			Document document = Jsoup.parse(crawlHttpClient.fetch(articleUrl.url(), siteConfig), articleUrl.url());
			Element contentElement = document.selectFirst(BODY_SELECTOR);
			String contentHtml = contentElement == null ? "<p>내용 없음</p>" : contentElement.html();
			String imageUrl = document.select(REPRESENTATIVE_IMAGE_SELECTOR).attr("abs:src");
			return new RawArticle(
				siteConfig.getSiteId(), articleUrl.externalId(), articleUrl.url(), articleUrl.category(),
				requiredText(document, TITLE_SELECTOR), contentHtml, AUTHOR, requiredArticleDate(articleUrl), imageUrl,
				extractAttachments(document));
		} catch (RuntimeException e) {
			log.error("[크롤링] SW교육원 공지 파싱 실패. crawlerType={}, siteId={}, url={}",
				CrawlerType.CAU_SW_EDU_NOTICE, siteConfig.getSiteId(), articleUrl.url(), e);
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	private String extractBoardId(String url) {
		Matcher matcher = BOARD_ID_PATTERN.matcher(url);
		if (!matcher.find()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return matcher.group(1);
	}

	private String requiredArticleDate(ArticleUrl articleUrl) {
		// 목록 수집 없이 상세만 처리하면 등록일을 알 수 없으므로 실패로 처리합니다.
		if (articleUrl.announcedAt() == null || articleUrl.announcedAt().isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return articleUrl.announcedAt();
	}

	private String requiredText(Element element, String selector) {
		Element selected = element.selectFirst(selector);
		if (selected == null || selected.text().isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return selected.text().trim();
	}

	private List<RawAttachment> extractAttachments(Document document) {
		Map<String, RawAttachment> attachmentsByUrl = new LinkedHashMap<>();
		for (Element element : document.select(ATTACHMENT_SELECTOR)) {
			String fileName = element.text().trim();
			String fileUrl = element.absUrl("href");
			if (!fileName.isBlank() && !fileUrl.isBlank()) {
				attachmentsByUrl.putIfAbsent(fileUrl, new RawAttachment(fileName, fileUrl));
			}
		}
		return List.copyOf(attachmentsByUrl.values());
	}
}
