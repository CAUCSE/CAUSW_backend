package net.causw.app.main.domain.integration.crawled.dto;

import java.time.LocalDate;
import java.util.List;

public record CleanArticle(
	String siteId,
	String targetBoardId,
	String externalId,
	String sourceUrl,
	String category,
	String title,
	String contentHtml,
	String author,
	LocalDate announceDate,
	String representativeImageUrl,
	List<CleanAttachment> attachments,
	String contentHash) {
}
