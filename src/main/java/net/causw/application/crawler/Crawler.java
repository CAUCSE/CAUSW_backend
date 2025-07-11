package net.causw.application.crawler;

import net.causw.adapter.persistence.crawled.CrawledNotice;
import java.util.List;

public interface Crawler {
    List<CrawledNotice> crawl();
}