package net.causw.app.main.domain.integration.crawled.core;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public record CrawlContext(SiteConfig siteConfig) {
	public CrawlContext {
		if (siteConfig == null) {
			throw new IllegalArgumentException("siteConfig must not be null");
		}
	}
}
