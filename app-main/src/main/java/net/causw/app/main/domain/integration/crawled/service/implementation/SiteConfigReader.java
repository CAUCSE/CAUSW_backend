package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.repository.SiteConfigRepository;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteConfigReader {
	private final SiteConfigRepository siteConfigRepository;

	/**
	 * 사이트 식별자로 활성화된 크롤링 설정을 조회합니다.
	 *
	 * @param siteId 사이트 식별자
	 * @return 활성화된 사이트 설정
	 */
	public SiteConfig getEnabledBySiteId(String siteId) {
		return siteConfigRepository.findById(siteId)
			.filter(SiteConfig::getIsEnabled)
			.orElseThrow(IntegrationErrorCode.CRAWL_SITE_CONFIG_NOT_FOUND::toBaseException);
	}

	/**
	 * 활성화된 모든 사이트 설정을 사이트 식별자 순으로 조회합니다.
	 *
	 * @return 활성화된 사이트 설정 목록
	 */
	public List<SiteConfig> findAllEnabled() {
		return siteConfigRepository.findAllByIsEnabledTrueOrderBySiteIdAsc();
	}
}
