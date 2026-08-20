package net.causw.app.main.domain.integration.crawled.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
	private static final int MAX_CONCURRENT_REQUESTS = 5;

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

		List<ArticleCrawlOutcome> outcomes = fetchAndCleanArticles(context, crawler, uniqueArticles.values());
		List<String> failedUrls = outcomes.stream()
			.filter(ArticleCrawlOutcome::isFailed)
			.map(outcome -> outcome.articleUrl().url())
			.toList();
		outcomes.stream()
			.filter(ArticleCrawlOutcome::isFailed)
			.forEach(outcome -> log.error("[크롤링] 공지 처리 실패. siteId={}, url={}", siteConfig.getSiteId(),
				outcome.articleUrl().url(), outcome.exception()));
		List<CleanArticle> cleanArticles = outcomes.stream()
			.map(ArticleCrawlOutcome::cleanArticle)
			.filter(java.util.Objects::nonNull)
			.toList();

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

	/**
	 * 공지 본문 요청과 정제를 virtual thread에서 병렬 수행합니다.
	 *
	 * <p>외부 I/O 작업만 병렬화하며, DB 반영은 호출 측에서 단일 트랜잭션으로 수행합니다.</p>
	 *
	 * @param context 사이트 설정을 포함한 실행 컨텍스트
	 * @param crawler 본문을 요청할 크롤러
	 * @param articleUrls 처리할 공지 URL 목록
	 * @return URL 순서를 유지한 공지별 처리 결과
	 */
	private List<ArticleCrawlOutcome> fetchAndCleanArticles(
		CrawlContext context,
		SiteCrawler crawler,
		Iterable<ArticleUrl> articleUrls) {
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);
			List<CompletableFuture<ArticleCrawlOutcome>> futures = new ArrayList<>();
			for (ArticleUrl articleUrl : articleUrls) {
				futures.add(CompletableFuture.supplyAsync(
					() -> fetchAndCleanWithPermit(context, crawler, articleUrl, semaphore), executor));
			}
			return futures.stream().map(CompletableFuture::join).toList();
		}
	}

	private ArticleCrawlOutcome fetchAndCleanWithPermit(
		CrawlContext context,
		SiteCrawler crawler,
		ArticleUrl articleUrl,
		Semaphore semaphore) {
		try {
			semaphore.acquire();
			RawArticle rawArticle;
			try {
				rawArticle = crawler.fetchArticle(context, articleUrl);
			} finally {
				semaphore.release();
			}
			return ArticleCrawlOutcome.success(articleUrl,
				crawledArticleCleaner.clean(rawArticle, context.siteConfig()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return ArticleCrawlOutcome.failure(articleUrl, new IllegalStateException("공지 요청이 중단되었습니다.", e));
		} catch (RuntimeException e) {
			return ArticleCrawlOutcome.failure(articleUrl, e);
		}
	}

	private record ArticleCrawlOutcome(ArticleUrl articleUrl, CleanArticle cleanArticle, RuntimeException exception) {
		private static ArticleCrawlOutcome success(ArticleUrl articleUrl, CleanArticle cleanArticle) {
			return new ArticleCrawlOutcome(articleUrl, cleanArticle, null);
		}

		private static ArticleCrawlOutcome failure(ArticleUrl articleUrl, RuntimeException exception) {
			return new ArticleCrawlOutcome(articleUrl, null, exception);
		}

		private boolean isFailed() {
			return exception != null;
		}
	}
}
