package net.causw.app.main.domain.integration.crawled.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
	List<CrawledNotice> findTop30ByOrderByAnnounceDateDesc();

	//링크로 공지 조회 (업데이트 감지용)
	Optional<CrawledNotice> findByLink(String link);

	/**
	 * 출처 사이트와 사이트 내부 식별자로 크롤링 공지를 조회합니다.
	 *
	 * @param siteId 출처 사이트 식별자
	 * @param externalId 사이트 내부 공지 식별자
	 * @return 조건에 일치하는 공지
	 */
	Optional<CrawledNotice> findBySiteIdAndExternalId(String siteId, String externalId);

	//업데이트된 공지들 조회 (배치 처리용)
	List<CrawledNotice> findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();
}
