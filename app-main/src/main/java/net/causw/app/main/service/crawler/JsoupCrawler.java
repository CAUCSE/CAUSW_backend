package net.causw.app.main.service.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.model.entity.crawled.CrawledFileLink;
import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JsoupCrawler implements Crawler {

	private final Random random = new Random();

	@Override
	public List<CrawledNotice> crawl() {
		int pageNum = 1;
		List<CrawledNotice> notices = new ArrayList<>();

		while (notices.size() < StaticValue.CRAWLING_MAX_NOTICES) {
			String url = StaticValue.CAU_CSE_BASE_URL + pageNum;
			Document doc = fetchUrlWithRetry(url);

			Elements rows = doc.select("table.table-basic tbody tr");
			if (rows.isEmpty()) {
				break;
			}

			for (Element row : rows) {
				if (notices.size() >= StaticValue.CRAWLING_MAX_NOTICES) {
					break;
				}

				Element titleElement = row.select("td.aleft a").first();
				if (titleElement == null) {
					continue;
				}

				String absoluteLink = titleElement.absUrl("href");
				notices.add(parseNotice(row, absoluteLink));

				sleep(StaticValue.CRAWLING_REQUEST_DELAY_MS);
			}
			pageNum++;
		}

		return notices;
	}

	private Document fetchUrlWithRetry(String url) {
		int retryCount = 0;
		while (retryCount < StaticValue.CRAWLING_MAX_RETRIES) {
			try {
				String userAgent = getRandomUserAgent();

				Document doc = Jsoup.connect(url)
					.userAgent(userAgent)
					.get();

				sleep(StaticValue.CRAWLING_REQUEST_DELAY_MS);
				return doc;

			} catch (IOException e) {
				retryCount++;

				if (retryCount >= StaticValue.CRAWLING_MAX_RETRIES) {
					log.error("최대 재시도 횟수 초과: {}", url);
					throw new InternalServerException(ErrorCode.INTERNAL_SERVER,
						MessageUtil.FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE);
				}

				int retryDelay = StaticValue.CRAWLING_REQUEST_DELAY_MS * retryCount;
				sleep(retryDelay);
			}
		}
		throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE);
	}

	private String getRandomUserAgent() {
		return StaticValue.CRAWLING_USER_AGENTS[random.nextInt(StaticValue.CRAWLING_USER_AGENTS.length)];
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, "크롤링이 중단되었습니다.");
		}
	}

	private CrawledNotice parseNotice(Element row, String noticeUrl) {
		String noticeType = row.select("td span.tag").text();
		Document detailDoc = fetchUrlWithRetry(noticeUrl);

		String title = detailDoc.select("div.header > h3").text();
		String announceDate = detailDoc.select("div.header > div > span").get(1).text();
		String author = detailDoc.select("div.header > div > span").get(3).text();
		String imageLink = detailDoc.select("div.fr-view > p > img").attr("abs:src");

		Element contentElement = detailDoc.select("div.fr-view").first();
		String contentHtml = contentElement != null ? contentElement.html() : "<p>내용 없음</p>";
		
		// 이미지 URL 정규화: HTTP -> HTTPS 변환, data-src -> src 치환
		contentHtml = normalizeImageUrls(contentHtml);

		List<CrawledFileLink> fileLinks = extractDownloadLink(detailDoc);
		return CrawledNotice.of(
			noticeType,
			title,
			contentHtml,
			noticeUrl,
			author,
			announceDate,
			imageLink,
			fileLinks.isEmpty() ? null : fileLinks
		);
	}

	private List<CrawledFileLink> extractDownloadLink(Document detailDoc) {
		List<CrawledFileLink> crawledFileLinks = new ArrayList<>();

		// 방법 1: JavaScript 함수 파싱
		Elements downloadLinks = detailDoc.select("div.files span");
		extractFileLinksFromElements(downloadLinks, crawledFileLinks);

		// 방법 2: 첨부파일 테이블 방식
		Elements fileTableRows = detailDoc.select("table.file-list tbody tr");
		extractFileLinksFromTable(fileTableRows, crawledFileLinks);

		// 방법 3: 직접 링크 방식
		Elements directLinks = detailDoc.select("div.fr-view a[href*='download.php']");
		extractDirectFileLinks(directLinks, crawledFileLinks);

		return removeDuplicateFileLinks(crawledFileLinks);
	}

	private void extractFileLinksFromElements(Elements downloadLinks, List<CrawledFileLink> fileLinks) {
		Pattern pattern = Pattern.compile("goLocation\\('/_module/bbs/download.php','(\\d+)','(\\w+)'\\).*?>(.*?)<");

		for (Element link : downloadLinks) {
			Matcher matcher = pattern.matcher(link.outerHtml());

			while (matcher.find()) {
				try {
					String uid = matcher.group(1);
					String code = matcher.group(2);
					String fileName = matcher.group(3).trim();

					if (isValidFileName(fileName)) {
						String fileUrl = String.format(StaticValue.CAU_CSE_DOWNLOAD_URL_FORMAT, uid, code);
						fileLinks.add(CrawledFileLink.of(fileName, fileUrl));
					}
				} catch (Exception e) {
					// 개별 첨부파일 파싱 실패 시 무시하고 계속 진행
				}
			}
		}
	}

	private void extractFileLinksFromTable(Elements fileTableRows, List<CrawledFileLink> fileLinks) {
		for (Element row : fileTableRows) {
			try {
				Element fileNameElement = row.select("td:first-child a").first();
				if (fileNameElement != null) {
					String fileName = fileNameElement.text().trim();
					String fileUrl = fileNameElement.absUrl("href");

					if (isValidFileName(fileName) && fileUrl.contains("download.php")) {
						fileLinks.add(CrawledFileLink.of(fileName, fileUrl));
					}
				}
			} catch (Exception e) {
				// 개별 첨부파일 파싱 실패 시 무시하고 계속 진행
			}
		}
	}

	private void extractDirectFileLinks(Elements directLinks, List<CrawledFileLink> fileLinks) {
		for (Element link : directLinks) {
			try {
				String fileName = link.text().trim();
				String fileUrl = link.absUrl("href");

				if (isValidFileName(fileName) && fileUrl.contains("download.php")) {
					fileLinks.add(CrawledFileLink.of(fileName, fileUrl));
				}
			} catch (Exception e) {
				// 개별 첨부파일 파싱 실패 시 무시하고 계속 진행
			}
		}
	}

	private boolean isValidFileName(String fileName) {
		return fileName != null &&
			!fileName.trim().isEmpty() &&
			!fileName.equals("첨부파일") &&
			!fileName.equals("파일") &&
			fileName.length() > 1;
	}

	private List<CrawledFileLink> removeDuplicateFileLinks(List<CrawledFileLink> fileLinks) {
		List<CrawledFileLink> uniqueLinks = new ArrayList<>();
		List<String> seenFileNames = new ArrayList<>();

		for (CrawledFileLink link : fileLinks) {
			if (!seenFileNames.contains(link.getFileName())) {
				uniqueLinks.add(link);
				seenFileNames.add(link.getFileName());
			}
		}

		return uniqueLinks;
	}

	// HTML 내 이미지 URL을 정규화
	private String normalizeImageUrls(String html) {
		if (html == null || html.isBlank()) {
			return html;
		}

		try {
			Document doc = Jsoup.parseBodyFragment(html);
			
			// 모든 img 태그 처리
			Elements images = doc.select("img");
			for (Element img : images) {
				// 1. src 우선순위: src > data-src > data-original > data-lazy
				String src = img.attr("src");
				if (src.isBlank()) {
					src = img.attr("data-src");
				}
				if (src.isBlank()) {
					src = img.attr("data-original");
				}
				if (src.isBlank()) {
					src = img.attr("data-lazy");
				}
				
				if (!src.isBlank()) {
					// 2. 절대경로로 변환
					String absoluteUrl = img.absUrl("src");
					if (absoluteUrl.isBlank()) {
						absoluteUrl = src;
					}
					
					// 3. HTTP -> HTTPS 변환
					String httpsUrl = forceHttps(absoluteUrl);
					
					// 4. src 속성 설정 및 data-* 속성 제거
					img.attr("src", httpsUrl);
					img.removeAttr("data-src");
					img.removeAttr("data-original");
					img.removeAttr("data-lazy");
					img.removeAttr("srcset");
				}
			}
			
			return doc.body().html();
		} catch (Exception e) {
			return html;
		}
	}

	// HTTP URL을 HTTPS로 변환합니다.
	private String forceHttps(String url) {
		if (url == null || url.isBlank()) {
			return url;
		}
		
		try {
			if (url.startsWith("http://")) {
				return url.replaceFirst("http://", "https://");
			}
			return url;
		} catch (Exception e) {
			return url;
		}
	}
}
