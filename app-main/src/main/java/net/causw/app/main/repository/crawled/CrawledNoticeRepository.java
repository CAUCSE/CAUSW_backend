package net.causw.app.main.repository.crawled;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
	List<CrawledNotice> findTop30ByOrderByAnnounceDateDesc();

	//링크로 공지 조회 (업데이트 감지용)
	Optional<CrawledNotice> findByLink(String link);

	//업데이트된 공지들 조회 (배치 처리용)
	List<CrawledNotice> findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();
}
