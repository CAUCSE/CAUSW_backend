package net.causw.adapter.persistence.repository;


import net.causw.adapter.persistence.crawled.CrawledNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
}
