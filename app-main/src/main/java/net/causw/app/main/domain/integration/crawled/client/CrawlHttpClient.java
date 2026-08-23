package net.causw.app.main.domain.integration.crawled.client;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public interface CrawlHttpClient {
	/**
	 * 사이트 설정의 요청 정책을 적용해 지정 URL의 HTML을 가져옵니다.
	 *
	 * @param url 요청할 URL
	 * @param siteConfig 요청 헤더, 지연, 타임아웃 및 재시도 설정
	 * @return 응답 HTML
	 */
	String fetch(String url, SiteConfig siteConfig);
}
