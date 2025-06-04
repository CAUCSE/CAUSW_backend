package net.causw.adapter.persistence.repository.crawled;


import net.causw.adapter.persistence.crawled.CrawledNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
    List<CrawledNotice> findTop20ByOrderByAnnounceDateDesc();
}
