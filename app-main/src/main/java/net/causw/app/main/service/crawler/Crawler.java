package net.causw.app.main.service.crawler;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import java.util.List;

public interface Crawler {
    List<CrawledNotice> crawl();
}