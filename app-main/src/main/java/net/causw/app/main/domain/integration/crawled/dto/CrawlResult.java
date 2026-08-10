package net.causw.app.main.domain.integration.crawled.dto;

import java.util.List;

public record CrawlResult(
	String siteId,
	int discoveredCount,
	int createdCount,
	int updatedCount,
	int unchangedCount,
	List<String> failedUrls) {

	/**
	 * 처리에 실패한 공지 개수를 반환합니다.
	 *
	 * @return 실패 URL 개수
	 */
	public int failedCount() {
		return failedUrls.size();
	}
}
