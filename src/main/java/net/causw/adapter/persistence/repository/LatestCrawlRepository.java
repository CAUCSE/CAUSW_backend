package net.causw.adapter.persistence.repository;

import jakarta.transaction.Transactional;
import jdk.jfr.Category;
import net.causw.adapter.persistence.crawled.LatestCrawl;
import net.causw.domain.model.enums.CrawlCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LatestCrawlRepository extends JpaRepository<LatestCrawl, String> {
    Optional<LatestCrawl> findByCrawlCategory(CrawlCategory crawlCategory);

    @Modifying
    @Transactional
    @Query("UPDATE LatestCrawl lc SET lc.latestUrl = :newUrl WHERE lc.crawlCategory = :crawlCategory")
    void updateLatestUrlByCategory(@Param("newUrl") String newUrl, @Param("crawlCategory") CrawlCategory crawlCategory);

}
