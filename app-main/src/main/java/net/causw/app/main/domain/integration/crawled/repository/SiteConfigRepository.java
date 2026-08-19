package net.causw.app.main.domain.integration.crawled.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, String> {
	/**
	 * 사이트 식별자로 활성화된 설정을 조회합니다.
	 *
	 * @param siteId 사이트 식별자
	 * @return 활성화된 사이트 설정
	 */
	Optional<SiteConfig> findBySiteIdAndIsEnabledTrue(String siteId);

	/**
	 * 활성화된 모든 사이트 설정을 사이트 식별자 오름차순으로 조회합니다.
	 *
	 * @return 활성화된 사이트 설정 목록
	 */
	List<SiteConfig> findAllByIsEnabledTrueOrderBySiteIdAsc();
}
