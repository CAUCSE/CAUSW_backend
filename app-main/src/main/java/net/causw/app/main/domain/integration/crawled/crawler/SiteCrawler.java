package net.causw.app.main.domain.integration.crawled.crawler;

import java.util.List;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;

public interface SiteCrawler {
	/**
	 * 이 구현체가 처리하는 크롤러 유형을 반환합니다.
	 *
	 * @return 크롤러 유형
	 */
	CrawlerType getCrawlerType();

	/**
	 * 사이트 공지 목록에서 수집할 원문 URL을 조회합니다.
	 *
	 * @param context 사이트 설정을 포함한 실행 컨텍스트
	 * @return 수집 대상 공지 URL 목록
	 */
	List<ArticleUrl> fetchList(CrawlContext context);

	/**
	 * 공지 원문을 조회하고 사이트별 구조에 맞춰 파싱합니다.
	 *
	 * @param context 사이트 설정을 포함한 실행 컨텍스트
	 * @param articleUrl 조회할 공지 URL 정보
	 * @return 정제 전 공지 데이터
	 */
	RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl);
}
