package net.causw.adapter.persistence.repository.crawled;


import net.causw.adapter.persistence.crawled.CrawledNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
}
