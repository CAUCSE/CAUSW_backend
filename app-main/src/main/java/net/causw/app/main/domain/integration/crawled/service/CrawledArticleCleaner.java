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
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;
import net.causw.global.constant.StaticValue;
import net.causw.global.util.HashUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CrawledArticleCleaner {
	private static final Safelist CONTENT_SAFELIST = Safelist.relaxed()
		.addTags("img")
		.addAttributes(":all", "class", "style")
		.addAttributes("img", "src", "alt", "title", "width", "height");

	public CleanArticle clean(RawArticle rawArticle, SiteConfig siteConfig) {
		try {
			String sourceUrl = normalizeUrl(rawArticle.sourceUrl(), siteConfig.getBaseUrl());
			String contentHtml = cleanContent(rawArticle.contentHtml(), sourceUrl);
			List<CleanAttachment> attachments = cleanAttachments(rawArticle.attachments(), sourceUrl);
			String imageUrl = normalizeNullableUrl(rawArticle.representativeImageUrl(), sourceUrl);
			LocalDate announceDate = LocalDate.parse(
				rawArticle.announcedAt().trim(),
				DateTimeFormatter.ofPattern(siteConfig.getSelectors().getDateFormat()));
			String title = requireText(rawArticle.title());
			String author = requireText(rawArticle.author());
			String category = rawArticle.category() == null ? "" : rawArticle.category().trim();
			String contentHash = generateContentHash(title, contentHtml, author, announceDate, imageUrl, attachments);

			return new CleanArticle(
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
			log.error(e.toString());
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

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

	private String generateContentHash(
		String title,
		String content,
		String author,
		LocalDate announceDate,
		String imageUrl,
		List<CleanAttachment> attachments) {
		StringBuilder value = new StringBuilder()
			.append(title).append('\u001f')
			.append(content).append('\u001f')
			.append(author).append('\u001f')
			.append(announceDate).append('\u001f')
			.append(imageUrl == null ? "" : imageUrl);
		attachments.forEach(attachment -> value.append('\u001e')
			.append(attachment.fileName()).append('\u001f').append(attachment.fileUrl()));
		return HashUtil.generateSHA256(value.toString());
	}

	private String normalizeNullableUrl(String url, String baseUrl) {
		return url == null || url.isBlank() ? null : normalizeUrl(url, baseUrl);
	}

	private String normalizeUrl(String url, String baseUrl) {
		URI normalized = URI.create(baseUrl).resolve(url.trim()).normalize();
		String result = normalized.toString();
		return result.startsWith("http://") ? "https://" + result.substring("http://".length()) : result;
	}

	private String requireText(String value) {
		if (value == null || value.isBlank()) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
		return value.trim();
	}
}
