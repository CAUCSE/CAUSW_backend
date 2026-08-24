package net.causw.app.main.domain.integration.crawled.service;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CleanAttachment;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawAttachment;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeContentHashManager;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawledArticleCleaner {
	private static final Safelist CONTENT_SAFELIST = Safelist.relaxed()
		.addTags("img")
		.addAttributes(":all", "class", "style")
		.addAttributes("img", "src", "alt", "title", "width", "height");
	private final CrawledNoticeContentHashManager crawledNoticeContentHashManager;

	/**
	 * 파싱된 원문 공지를 정규화하고 저장 가능한 데이터로 변환합니다.
	 *
	 * @param rawArticle 사이트에서 파싱한 원문 공지
	 * @param siteConfig 날짜 형식과 기본 URL을 포함한 사이트 설정
	 * @return 정제된 공지
	 */
	public CleanArticle clean(RawArticle rawArticle, SiteConfig siteConfig) {
		try {
			String sourceUrl = normalizeUrl(rawArticle.sourceUrl(), siteConfig.getBaseUrl());
			String contentHtml = cleanContent(rawArticle.contentHtml(), sourceUrl);
			List<CleanAttachment> attachments = cleanAttachments(rawArticle.attachments(), sourceUrl);
			String imageUrl = normalizeNullableUrl(rawArticle.representativeImageUrl(), sourceUrl);
			LocalDate announceDate = LocalDate.parse(
				rawArticle.announcedAt().trim(),
				DateTimeFormatter.ofPattern(siteConfig.getDateFormat()));
			String title = requireText(rawArticle.title());
			String author = requireText(rawArticle.author());
			String category = rawArticle.category() == null ? "" : rawArticle.category().trim();
			String contentHash = crawledNoticeContentHashManager.generate(
				title, contentHtml, author, announceDate, imageUrl, attachments);

			return CleanArticle.of(
				rawArticle.siteId(),
				siteConfig.getTargetBoardId(),
				requireText(rawArticle.externalId()),
				sourceUrl,
				category,
				title,
				contentHtml,
				author,
				announceDate,
				imageUrl,
				attachments,
				contentHash);
		} catch (RuntimeException e) {
			log.error("[크롤링] 공지 정제 실패. siteId={}, externalId={}",
				rawArticle.siteId(), rawArticle.externalId(), e);
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	/**
	 * 본문 HTML의 이미지 URL을 정규화하고 허용되지 않은 요소와 속성을 제거합니다.
	 *
	 * @param html 정제할 본문 HTML
	 * @param baseUrl 상대 URL 해석에 사용할 URL
	 * @return 정제된 본문 HTML
	 */
	private String cleanContent(String html, String baseUrl) {
		Document document = Jsoup.parseBodyFragment(html == null ? "" : html, baseUrl);
		for (Element image : document.select("img")) {
			StaticValue.IMAGE_SRC_ATTRIBUTES.stream()
				.filter(attribute -> !image.attr(attribute).isBlank())
				.findFirst()
				.ifPresent(attribute -> image.attr("src", normalizeUrl(image.attr(attribute), baseUrl)));
			StaticValue.REMOVABLE_IMAGE_ATTRIBUTES.forEach(image::removeAttr);
		}
		Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
		return Jsoup.clean(document.body().html(), baseUrl, CONTENT_SAFELIST, outputSettings).trim();
	}

	/**
	 * 첨부파일명을 정리하고 URL을 정규화한 뒤 중복을 제거합니다.
	 *
	 * @param rawAttachments 원문 첨부파일 목록
	 * @param baseUrl 상대 URL 해석에 사용할 URL
	 * @return URL 순으로 정렬된 첨부파일 목록
	 */
	private List<CleanAttachment> cleanAttachments(List<RawAttachment> rawAttachments, String baseUrl) {
		Map<String, CleanAttachment> attachmentsByUrl = new LinkedHashMap<>();
		if (rawAttachments != null) {
			for (RawAttachment attachment : rawAttachments) {
				if (attachment == null || attachment.fileName() == null || attachment.fileName().isBlank()
					|| attachment.fileUrl() == null || attachment.fileUrl().isBlank()) {
					continue;
				}
				String url = normalizeUrl(attachment.fileUrl(), baseUrl);
				attachmentsByUrl.putIfAbsent(url, new CleanAttachment(attachment.fileName().trim(), url));
			}
		}
		return attachmentsByUrl.values().stream()
			.sorted(Comparator.comparing(CleanAttachment::fileUrl))
			.toList();
	}

	/**
	 * 선택적 URL 값이 존재하면 절대 URL로 정규화합니다.
	 *
	 * @param url 정규화할 URL
	 * @param baseUrl 상대 URL 해석에 사용할 URL
	 * @return 정규화된 URL 또는 {@code null}
	 */
	private String normalizeNullableUrl(String url, String baseUrl) {
		return url == null || url.isBlank() ? null : normalizeUrl(url, baseUrl);
	}

	/**
	 * 상대 URL을 절대 URL로 변환하고 HTTP 스킴을 HTTPS로 통일합니다.
	 *
	 * @param url 정규화할 URL
	 * @param baseUrl 상대 URL 해석에 사용할 URL
	 * @return 정규화된 절대 URL
	 */
	private String normalizeUrl(String url, String baseUrl) {
		URI normalized = URI.create(baseUrl).resolve(url.trim()).normalize();
		String scheme = normalized.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		String result = normalized.toString();
		return result.startsWith("http://") ? "https://" + result.substring("http://".length()) : result;
	}

	/**
	 * 필수 문자열이 비어 있지 않은지 확인하고 바깥 공백을 제거합니다.
	 *
	 * @param value 확인할 문자열
	 * @return 공백을 제거한 문자열
	 */
	private String requireText(String value) {
		if (value == null || value.isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return value.trim();
	}
}
