package net.causw.app.main.domain.integration.crawled.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, String> {
	Optional<SiteConfig> findBySiteIdAndIsEnabledTrue(String siteId);

	List<SiteConfig> findAllByIsEnabledTrueOrderBySiteIdAsc();
}
