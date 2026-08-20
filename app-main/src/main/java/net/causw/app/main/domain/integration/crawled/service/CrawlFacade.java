package net.causw.app.main.domain.integration.crawled.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.core.SiteCrawlerRegistry;
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.implementation.SiteConfigReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 사이트별 공지 크롤링, 정제 및 저장 흐름을 조율하는 파사드입니다.
 *
 * <p>외부 사이트 통신은 트랜잭션 밖에서 처리하고, 정제된 결과의 DB 반영은
 * {@link CrawledNoticePersistenceService}에 위임합니다.</p>
 */
public class CrawlFacade {
	private final SiteCrawlerRegistry siteCrawlerRegistry;
	private final SiteConfigReader siteConfigReader;
	private final CrawledArticleCleaner crawledArticleCleaner;
	private final CrawledNoticePersistenceService crawledNoticePersistenceService;

	/**
	 * 지정한 활성 사이트의 공지를 수집하고 저장합니다.
	 *
	 * @param siteId 크롤링할 사이트 식별자
	 * @return 사이트별 크롤링 결과
	 */
	public CrawlResult crawl(String siteId) {
		return crawl(siteConfigReader.getEnabledBySiteId(siteId));
	}

	/**
	 * 활성화된 모든 사이트를 크롤링하며 사이트별 실패를 격리합니다.
	 *
	 * @return 성공적으로 실행된 사이트별 크롤링 결과
	 */
	public List<CrawlResult> crawlAllEnabled() {
		List<CrawlResult> results = new ArrayList<>();
		for (SiteConfig siteConfig : siteConfigReader.findAllEnabled()) {
			try {
				results.add(crawl(siteConfig));
			} catch (RuntimeException e) {
				log.error("[크롤링] 사이트 수집 실패. siteId={}", siteConfig.getSiteId(), e);
			}
		}
		return List.copyOf(results);
	}

	private CrawlResult crawl(SiteConfig siteConfig) {
		CrawlContext context = new CrawlContext(siteConfig);
		return crawl(context, siteCrawlerRegistry.get(siteConfig.getCrawlerType()));
	}

	private CrawlResult crawl(CrawlContext context, SiteCrawler crawler) {
		SiteConfig siteConfig = context.siteConfig();
		Map<String, ArticleUrl> uniqueArticles = new LinkedHashMap<>();
		crawler.fetchList(context).forEach(article -> uniqueArticles.putIfAbsent(article.externalId(), article));

		List<String> failedUrls = new ArrayList<>();
		List<CleanArticle> cleanArticles = new ArrayList<>();
		for (ArticleUrl articleUrl : uniqueArticles.values()) {
			try {
				RawArticle rawArticle = crawler.fetchArticle(context, articleUrl);
				cleanArticles.add(crawledArticleCleaner.clean(rawArticle, siteConfig));
			} catch (RuntimeException e) {
				failedUrls.add(articleUrl.url());
				log.error("[크롤링] 공지 처리 실패. siteId={}, url={}", siteConfig.getSiteId(), articleUrl.url(), e);
			}
		}

		Map<String, CrawlSaveStatus> saveStatuses = crawledNoticePersistenceService.persistAll(siteConfig.getSiteId(),
			cleanArticles);
		return new CrawlResult(
			siteConfig.getSiteId(),
			uniqueArticles.size(),
			countByStatus(saveStatuses, CrawlSaveStatus.CREATED),
			countByStatus(saveStatuses, CrawlSaveStatus.UPDATED),
			countByStatus(saveStatuses, CrawlSaveStatus.UNCHANGED),
			List.copyOf(failedUrls));
	}

	private int countByStatus(Map<String, CrawlSaveStatus> saveStatuses, CrawlSaveStatus expectedStatus) {
		return (int)saveStatuses.values().stream().filter(expectedStatus::equals).count();
	}
}
