package net.causw.app.main.domain.integration.crawled.dto;

import java.util.List;

public record CrawlResult(
	String siteId,
	int discoveredCount,
	int createdCount,
	int updatedCount,
	int unchangedCount,
	List<String> failedUrls) {

	public int failedCount() {
		return failedUrls.size();
	}
}
