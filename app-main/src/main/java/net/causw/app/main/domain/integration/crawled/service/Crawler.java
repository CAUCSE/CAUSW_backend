package net.causw.app.main.domain.integration.crawled.service;

import java.util.List;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;

public interface Crawler {
	List<CrawledNotice> crawl();
}