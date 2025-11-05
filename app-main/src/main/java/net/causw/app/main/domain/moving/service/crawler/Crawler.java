package net.causw.app.main.domain.moving.service.crawler;

import java.util.List;

import net.causw.app.main.domain.moving.model.entity.crawled.CrawledNotice;

public interface Crawler {
	List<CrawledNotice> crawl();
}