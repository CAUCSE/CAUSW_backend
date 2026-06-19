package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;

public interface Crawler {
	List<CrawledNotice> crawl();
}
