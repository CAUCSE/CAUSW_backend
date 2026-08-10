package net.causw.app.main.domain.integration.crawled.core;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public record CrawlContext(SiteConfig siteConfig) {
	/**
	 * 크롤링 실행 컨텍스트를 생성합니다.
	 *
	 * @param siteConfig 실행에 사용할 사이트 설정
	 * @throws IllegalArgumentException 사이트 설정이 {@code null}인 경우
	 */
	public CrawlContext {
		if (siteConfig == null) {
			throw new IllegalArgumentException("siteConfig must not be null");
		}
	}
}
