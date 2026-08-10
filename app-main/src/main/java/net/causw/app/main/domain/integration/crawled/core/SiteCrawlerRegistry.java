package net.causw.app.main.domain.integration.crawled.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

@Component
public class SiteCrawlerRegistry {
	private final Map<CrawlerType, SiteCrawler> crawlersByType;

	public SiteCrawlerRegistry(List<SiteCrawler> crawlers) {
		this.crawlersByType = crawlers.stream()
			.collect(Collectors.toUnmodifiableMap(
				SiteCrawler::getCrawlerType,
				crawler -> crawler));
	}

	public SiteCrawler get(CrawlerType crawlerType) {
		SiteCrawler crawler = crawlersByType.get(crawlerType);
		if (crawler == null) {
			throw IntegrationErrorCode.CRAWLER_NOT_FOUND.toBaseException();
		}
		return crawler;
	}

	public List<SiteCrawler> getAll() {
		return List.copyOf(crawlersByType.values());
	}
}
