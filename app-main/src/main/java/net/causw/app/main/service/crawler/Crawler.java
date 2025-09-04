package net.causw.app.main.service.crawler;

import java.util.List;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;

public interface Crawler {
	List<CrawledNotice> crawl();
}