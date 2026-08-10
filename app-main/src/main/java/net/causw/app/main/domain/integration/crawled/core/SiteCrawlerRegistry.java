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

	/**
	 * 등록된 크롤러를 유형별 불변 맵으로 구성합니다.
	 *
	 * @param crawlers 애플리케이션에 등록된 사이트 크롤러 목록
	 * @throws IllegalStateException 같은 유형의 크롤러가 둘 이상 등록된 경우
	 */
	public SiteCrawlerRegistry(List<SiteCrawler> crawlers) {
		this.crawlersByType = crawlers.stream()
			.collect(Collectors.toUnmodifiableMap(
				SiteCrawler::getCrawlerType,
				crawler -> crawler));
	}

	/**
	 * 지정 유형을 처리하는 크롤러를 조회합니다.
	 *
	 * @param crawlerType 조회할 크롤러 유형
	 * @return 등록된 사이트 크롤러
	 */
	public SiteCrawler get(CrawlerType crawlerType) {
		SiteCrawler crawler = crawlersByType.get(crawlerType);
		if (crawler == null) {
			throw IntegrationErrorCode.CRAWLER_NOT_FOUND.toBaseException();
		}
		return crawler;
	}

	/**
	 * 등록된 모든 사이트 크롤러를 반환합니다.
	 *
	 * @return 수정할 수 없는 크롤러 목록
	 */
	public List<SiteCrawler> getAll() {
		return List.copyOf(crawlersByType.values());
	}
}
