package net.causw.app.main.domain.integration.crawled.core;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

public record CrawlContext(SiteConfig siteConfig) {
	/**
	 * 크롤링 실행 컨텍스트를 생성합니다.
	 *
	 * @param siteConfig 실행에 사용할 사이트 설정
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 사이트 설정이 {@code null}인 경우
	 */
	public CrawlContext {
		if (siteConfig == null) {
			throw IntegrationErrorCode.CRAWL_SITE_CONFIG_NOT_FOUND.toBaseException();
		}
	}
}
