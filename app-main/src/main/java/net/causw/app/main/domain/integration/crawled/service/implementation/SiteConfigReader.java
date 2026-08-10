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

	public SiteConfig getEnabledBySiteId(String siteId) {
		return siteConfigRepository.findBySiteIdAndIsEnabledTrue(siteId)
			.orElseThrow(IntegrationErrorCode.CRAWL_SITE_CONFIG_NOT_FOUND::toBaseException);
	}

	public List<SiteConfig> findAllEnabled() {
		return siteConfigRepository.findAllByIsEnabledTrueOrderBySiteIdAsc();
	}
}
