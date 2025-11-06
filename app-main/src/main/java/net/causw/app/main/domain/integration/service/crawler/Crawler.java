package net.causw.app.main.domain.integration.service.crawler;

import java.util.List;

import net.causw.app.main.domain.integration.entity.crawled.CrawledNotice;

public interface Crawler {
	List<CrawledNotice> crawl();
}