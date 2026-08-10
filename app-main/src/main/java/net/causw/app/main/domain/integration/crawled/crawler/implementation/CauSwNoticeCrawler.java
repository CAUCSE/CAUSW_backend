package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import java.util.ArrayList;
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
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CauSwNoticeCrawler implements SiteCrawler {
	private static final Pattern SCRIPT_DOWNLOAD_PATTERN = Pattern.compile(
		"goLocation\\('/_module/bbs/download.php','(\\d+)','(\\w+)'\\).*?>(.*?)<");

	private final CrawlHttpClient crawlHttpClient;

	@Override
	public CrawlerType getCrawlerType() {
		return CrawlerType.CAU_SW_NOTICE;
	}

	@Override
	public List<ArticleUrl> fetchList(CrawlContext context) {
		SiteConfig siteConfig = context.siteConfig();
		List<ArticleUrl> articleUrls = new ArrayList<>();
		SiteSelectors selectors = siteConfig.getSelectors();

		for (int page = 1; page <= siteConfig.getMaxPages()
			&& articleUrls.size() < siteConfig.getMaxArticles(); page++) {
			String listUrl = siteConfig.getListUrl() + page;
			Document document = Jsoup.parse(crawlHttpClient.fetch(listUrl, siteConfig), listUrl);
			Elements rows = document.select(selectors.getArticleRow());
			if (rows.isEmpty()) {
				break;
			}

			for (Element row : rows) {
				if (articleUrls.size() >= siteConfig.getMaxArticles()) {
					break;
				}
				Element linkElement = row.selectFirst(selectors.getArticleLink());
				if (linkElement == null) {
					continue;
				}
				String url = linkElement.absUrl("href");
				if (url.isBlank()) {
					continue;
				}
				String category = row.select(selectors.getArticleCategory()).text();
				articleUrls.add(new ArticleUrl(url, url, category));
			}
		}

		return articleUrls.stream().distinct().toList();
	}

	@Override
	public RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl) {
		try {
			SiteConfig siteConfig = context.siteConfig();
			String html = crawlHttpClient.fetch(articleUrl.url(), siteConfig);
			Document document = Jsoup.parse(html, articleUrl.url());
			SiteSelectors selectors = siteConfig.getSelectors();
			Element contentElement = document.selectFirst(selectors.getBody());
			String contentHtml = contentElement == null ? "<p>내용 없음</p>" : contentElement.html();
			String imageUrl = document.select(selectors.getRepresentativeImage()).attr("abs:src");

			return new RawArticle(
				siteConfig.getSiteId(),
				articleUrl.externalId(),
				articleUrl.url(),
				removeNewBadge(articleUrl.category()),
				requiredText(document, selectors.getTitle()),
				contentHtml,
				requiredText(document, selectors.getAuthor()),
				requiredText(document, selectors.getDate()),
				imageUrl,
				extractAttachments(document));
		} catch (RuntimeException e) {
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
		extractScriptAttachments(document.select("div.files span"), attachments);
		extractLinkAttachments(document.select("table.file-list tbody tr td:first-child a"), attachments);
		extractLinkAttachments(document.select("div.fr-view a[href*='download.php']"), attachments);
		return List.copyOf(attachments.values());
	}

	private void extractScriptAttachments(Elements elements, Map<String, RawAttachment> attachments) {
		for (Element element : elements) {
			Matcher matcher = SCRIPT_DOWNLOAD_PATTERN.matcher(element.outerHtml());
			while (matcher.find()) {
				String fileName = matcher.group(3).trim();
				if (isValidFileName(fileName)) {
					String fileUrl = String.format(StaticValue.CAU_CSE_DOWNLOAD_URL_FORMAT,
						matcher.group(1), matcher.group(2));
					attachments.putIfAbsent(fileUrl, new RawAttachment(fileName, fileUrl));
				}
			}
		}
	}

	private void extractLinkAttachments(Elements elements, Map<String, RawAttachment> attachments) {
		for (Element element : elements) {
			String fileName = element.text().trim();
			String fileUrl = element.absUrl("href");
			if (isValidFileName(fileName) && fileUrl.contains("download.php")) {
				attachments.putIfAbsent(fileUrl, new RawAttachment(fileName, fileUrl));
			}
		}
	}

	private boolean isValidFileName(String fileName) {
		return fileName != null && !fileName.isBlank() && !fileName.equals("첨부파일") && !fileName.equals("파일");
	}

	private String removeNewBadge(String category) {
		return category == null ? "" : category.replace("NEW", "").trim();
	}
}
