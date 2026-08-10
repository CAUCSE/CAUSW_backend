package net.causw.app.main.domain.integration.crawled.client;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public interface CrawlHttpClient {
	String fetch(String url, SiteConfig siteConfig);
}
