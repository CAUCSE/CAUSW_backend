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
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.SiteConfigReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {
	private final SiteCrawlerRegistry siteCrawlerRegistry;
	private final SiteConfigReader siteConfigReader;
	private final CrawledArticleCleaner crawledArticleCleaner;
	private final CrawledNoticeWriter crawledNoticeWriter;

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

	/**
	 * 사이트 설정에 대응하는 크롤러를 선택해 수집을 실행합니다.
	 *
	 * @param siteConfig 실행할 사이트 설정
	 * @return 사이트별 크롤링 결과
	 */
	private CrawlResult crawl(SiteConfig siteConfig) {
		CrawlContext context = new CrawlContext(siteConfig);

		return crawl(context, siteCrawlerRegistry.get(siteConfig.getCrawlerType()));
	}

	/**
	 * 공지 목록을 중복 제거한 뒤 각 공지를 파싱, 정제하고 저장합니다.
	 *
	 * @param context 사이트 설정을 포함한 실행 컨텍스트
	 * @param crawler 사이트 구조를 처리할 크롤러
	 * @return 생성, 수정, 미변경 및 실패 건수를 포함한 결과
	 */
	private CrawlResult crawl(CrawlContext context, SiteCrawler crawler) {
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
				log.error("[크롤링] 공지 처리 실패. siteId={}, url={}",
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
