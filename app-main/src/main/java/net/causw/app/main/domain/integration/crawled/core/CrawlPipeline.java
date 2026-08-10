package net.causw.app.main.domain.integration.crawled.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.CrawledArticleCleaner;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.SiteConfigReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlPipeline {
	private final SiteCrawlerRegistry siteCrawlerRegistry;
	private final SiteConfigReader siteConfigReader;
	private final CrawledArticleCleaner crawledArticleCleaner;
	private final CrawledNoticeWriter crawledNoticeWriter;

	public CrawlResult run(String siteId) {
		SiteConfig siteConfig = siteConfigReader.getEnabledBySiteId(siteId);
		CrawlContext context = new CrawlContext(siteConfig);
		return run(context, siteCrawlerRegistry.get(siteConfig.getCrawlerType()));
	}

	public CrawlResult run(CrawlContext context, SiteCrawler crawler) {
		SiteConfig siteConfig = context.siteConfig();
		Map<String, ArticleUrl> uniqueArticles = new LinkedHashMap<>();
		crawler.fetchList(context).forEach(article -> uniqueArticles.putIfAbsent(article.externalId(), article));

		int created = 0;
		int updated = 0;
		int unchanged = 0;
		List<String> failedUrls = new ArrayList<>();

		for (ArticleUrl articleUrl : uniqueArticles.values()) {
			try {
				RawArticle rawArticle = crawler.fetchArticle(context, articleUrl);
				CleanArticle cleanArticle = crawledArticleCleaner.clean(rawArticle, siteConfig);
				CrawlSaveStatus status = crawledNoticeWriter.upsert(cleanArticle);
				switch (status) {
					case CREATED -> created++;
					case UPDATED -> updated++;
					case UNCHANGED -> unchanged++;
				}
			} catch (RuntimeException e) {
				failedUrls.add(articleUrl.url());
				log.error("[Crawl] Article processing failed. siteId={}, url={}",
					siteConfig.getSiteId(), articleUrl.url(), e);
			}
		}

		return new CrawlResult(
			siteConfig.getSiteId(),
			uniqueArticles.size(),
			created,
			updated,
			unchanged,
			List.copyOf(failedUrls));
	}
}
