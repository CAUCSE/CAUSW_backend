package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import net.causw.app.main.domain.integration.crawled.entity.SiteSelectors;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CauAiNoticeCrawler implements SiteCrawler {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter LIST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final Pattern NOTICE_NO_PATTERN = Pattern.compile("[?&]no=(\\d+)");

	private final CrawlHttpClient crawlHttpClient;

	@Override
	public CrawlerType getCrawlerType() {
		return CrawlerType.CAU_AI_NOTICE;
	}

	@Override
	public List<ArticleUrl> fetchList(CrawlContext context) {
		SiteConfig siteConfig = context.siteConfig();
		Map<String, ArticleUrl> articleUrlsByExternalId = new LinkedHashMap<>();
		SiteSelectors selectors = siteConfig.getSelectors();
		log.debug("[크롤링] AI학과 공지 목록 수집 시작. siteId={}, maxPages={}, maxArticles={}",
			siteConfig.getSiteId(), siteConfig.getMaxPages(), siteConfig.getMaxArticles());

		for (int page = 1; page <= siteConfig.getMaxPages()
			&& articleUrlsByExternalId.size() < siteConfig.getMaxArticles(); page++) {
			String listUrl = siteConfig.getListUrl() + page;
			Document document = Jsoup.parse(crawlHttpClient.fetch(listUrl, siteConfig), listUrl);
			Elements rows = document.select(selectors.getArticleRow());
			if (rows.isEmpty()) {
				break;
			}

			for (Element row : rows) {
				if (articleUrlsByExternalId.size() >= siteConfig.getMaxArticles()) {
					break;
				}
				if (!isWithinScanRange(row, siteConfig)) {
					continue;
				}
				Element linkElement = row.selectFirst(selectors.getArticleLink());
				if (linkElement == null) {
					continue;
				}
				String url = linkElement.absUrl("href");
				if (url.isBlank()) {
					continue;
				}
				String externalId = extractNoticeId(url);
				articleUrlsByExternalId.putIfAbsent(externalId,
					new ArticleUrl(url, externalId, row.select(selectors.getArticleCategory()).text()));
			}
		}

		return List.copyOf(articleUrlsByExternalId.values());
	}

	private boolean isWithinScanRange(Element row, SiteConfig siteConfig) {
		Element dateElement = row.selectFirst("td:nth-last-child(2)");
		if (dateElement == null || dateElement.text().isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		try {
			LocalDate announceDate = LocalDateTime.parse(dateElement.text().trim(), LIST_DATE_FORMATTER)
				.toLocalDate();
			LocalDate today = LocalDate.now(KOREA_ZONE_ID);
			return !announceDate.isBefore(today.minusDays(siteConfig.getMaxScanRangeDays()))
				&& !announceDate.isAfter(today);
		} catch (DateTimeParseException e) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	private String extractNoticeId(String url) {
		Matcher matcher = NOTICE_NO_PATTERN.matcher(url);
		if (!matcher.find()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return matcher.group(1);
	}

	@Override
	public RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl) {
		SiteConfig siteConfig = context.siteConfig();
		try {
			Document document = Jsoup.parse(crawlHttpClient.fetch(articleUrl.url(), siteConfig), articleUrl.url());
			SiteSelectors selectors = siteConfig.getSelectors();
			Element contentElement = document.selectFirst(selectors.getBody());
			String contentHtml = contentElement == null ? "<p>내용 없음</p>" : contentElement.html();
			String imageUrl = document.select(selectors.getRepresentativeImage()).attr("abs:src");
			return new RawArticle(
				siteConfig.getSiteId(), articleUrl.externalId(), articleUrl.url(), articleUrl.category(),
				requiredText(document, selectors.getTitle()), contentHtml,
				requiredText(document, selectors.getAuthor()), requiredText(document, selectors.getDate()), imageUrl,
				extractAttachments(document));
		} catch (RuntimeException e) {
			log.error("[크롤링] AI학과 공지 파싱 실패. crawlerType={}, siteId={}, url={}",
				CrawlerType.CAU_AI_NOTICE, siteConfig.getSiteId(), articleUrl.url(), e);
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	private String requiredText(Document document, String selector) {
		Element element = document.selectFirst(selector);
		if (element == null || element.text().isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return element.text();
	}

	private List<RawAttachment> extractAttachments(Document document) {
		Map<String, RawAttachment> attachments = new LinkedHashMap<>();
		addAttachments(document.select("div.files a[href], div.fr-view a[href*='download']"), attachments);
		return List.copyOf(attachments.values());
	}

	private void addAttachments(Elements elements, Map<String, RawAttachment> attachments) {
		for (Element element : elements) {
			String fileName = element.text().trim();
			String fileUrl = element.absUrl("href");
			if (!fileName.isBlank() && !fileName.equals("첨부파일") && !fileName.equals("파일") && !fileUrl.isBlank()) {
				attachments.putIfAbsent(fileUrl, new RawAttachment(fileName, fileUrl));
			}
		}
	}
}
