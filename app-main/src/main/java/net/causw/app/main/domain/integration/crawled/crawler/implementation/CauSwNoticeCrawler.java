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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CauSwNoticeCrawler implements SiteCrawler {
	private static final Pattern SCRIPT_DOWNLOAD_PATTERN = Pattern.compile(
		"goLocation\\('/_module/bbs/download.php','(\\d+)','(\\w+)'\\).*?>(.*?)<");
	private static final Pattern NOTICE_CODE_PATTERN = Pattern.compile("[?&]code=([^&]+)");
	private static final Pattern NOTICE_UID_PATTERN = Pattern.compile("[?&]uid=(\\d+)");

	private final CrawlHttpClient crawlHttpClient;

	/** {@inheritDoc} */
	@Override
	public CrawlerType getCrawlerType() {
		return CrawlerType.CAU_SW_NOTICE;
	}

	/** {@inheritDoc} */
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
				articleUrls.add(new ArticleUrl(url, extractNoticeId(url), category));
			}
		}

		return articleUrls.stream().distinct().toList();
	}

	private String extractNoticeId(String url) {
		Matcher codeMatcher = NOTICE_CODE_PATTERN.matcher(url);
		Matcher uidMatcher = NOTICE_UID_PATTERN.matcher(url);
		if (!codeMatcher.find() || !uidMatcher.find()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return codeMatcher.group(1) + ":" + uidMatcher.group(1);
	}

	/** {@inheritDoc} */
	@Override
	public RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl) {
		SiteConfig siteConfig = context.siteConfig();
		String html = crawlHttpClient.fetch(articleUrl.url(), siteConfig);
		try {
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
			log.error("[크롤링] 공지 파싱 실패. siteId={}, url={}", siteConfig.getSiteId(), articleUrl.url(), e);
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	/**
	 * 문서에서 필수 셀렉터의 텍스트를 추출합니다.
	 *
	 * @param document 파싱된 HTML 문서
	 * @param selector 텍스트를 찾을 CSS 셀렉터
	 * @return 공백을 제외한 요소 텍스트
	 */
	private String requiredText(Document document, String selector) {
		Element element = document.selectFirst(selector);
		if (element == null || element.text().isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return element.text();
	}

	/**
	 * 공지 문서에 포함된 스크립트형 및 링크형 첨부파일을 추출합니다.
	 *
	 * @param document 파싱된 공지 문서
	 * @return URL 기준으로 중복 제거된 첨부파일 목록
	 */
	private List<RawAttachment> extractAttachments(Document document) {
		Map<String, RawAttachment> attachments = new LinkedHashMap<>();
		extractScriptAttachments(document.select("div.files span"), attachments);
		extractLinkAttachments(document.select("table.file-list tbody tr td:first-child a"), attachments);
		extractLinkAttachments(document.select("div.fr-view a[href*='download.php']"), attachments);
		return List.copyOf(attachments.values());
	}

	/**
	 * 다운로드 스크립트가 포함된 요소에서 첨부파일을 추출합니다.
	 *
	 * @param elements 다운로드 스크립트 후보 요소
	 * @param attachments 추출 결과를 누적할 URL별 첨부파일 맵
	 */
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

	/**
	 * 다운로드 링크 요소에서 첨부파일을 추출합니다.
	 *
	 * @param elements 다운로드 링크 후보 요소
	 * @param attachments 추출 결과를 누적할 URL별 첨부파일 맵
	 */
	private void extractLinkAttachments(Elements elements, Map<String, RawAttachment> attachments) {
		for (Element element : elements) {
			String fileName = element.text().trim();
			String fileUrl = element.absUrl("href");
			if (isValidFileName(fileName) && fileUrl.contains("download.php")) {
				attachments.putIfAbsent(fileUrl, new RawAttachment(fileName, fileUrl));
			}
		}
	}

	/**
	 * 추출된 첨부파일명이 저장 가능한 값인지 확인합니다.
	 *
	 * @param fileName 확인할 첨부파일명
	 * @return 유효한 첨부파일명이면 {@code true}
	 */
	private boolean isValidFileName(String fileName) {
		return fileName != null && !fileName.isBlank() && !fileName.equals("첨부파일") && !fileName.equals("파일");
	}

	/**
	 * 카테고리 문자열에 포함된 신규 공지 배지를 제거합니다.
	 *
	 * @param category 원본 카테고리
	 * @return 신규 배지와 바깥 공백을 제거한 카테고리
	 */
	private String removeNewBadge(String category) {
		return category == null ? "" : category.replace("NEW", "").trim();
	}
}
