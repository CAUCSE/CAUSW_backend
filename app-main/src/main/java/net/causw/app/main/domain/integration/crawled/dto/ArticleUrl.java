package net.causw.app.main.domain.integration.crawled.dto;

public record ArticleUrl(String url, String externalId, String category, String announcedAt) {
	public ArticleUrl(String url, String externalId, String category) {
		this(url, externalId, category, null);
	}
}
