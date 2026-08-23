package net.causw.app.main.domain.integration.crawled.dto;

import java.util.List;

public record RawArticle(
	String siteId,
	String externalId,
	String sourceUrl,
	String category,
	String title,
	String contentHtml,
	String author,
	String announcedAt,
	String representativeImageUrl,
	List<RawAttachment> attachments) {
}
