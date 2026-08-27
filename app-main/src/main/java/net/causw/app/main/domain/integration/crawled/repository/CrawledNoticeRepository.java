package net.causw.app.main.domain.integration.crawled.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.query.PostCategoryBackfillTarget;

public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, String> {
	List<CrawledNotice> findTop30ByOrderByAnnounceDateDesc();

	//링크로 공지 조회 (업데이트 감지용)
	Optional<CrawledNotice> findByLink(String link);

	@EntityGraph(attributePaths = {"post", "crawledFileLinks"})
	List<CrawledNotice> findBySiteIdAndExternalIdIn(String siteId, List<String> externalIds);

	@EntityGraph(attributePaths = "crawledFileLinks")
	Optional<CrawledNotice> findByPostId(String postId);

	//업데이트된 공지들 조회 (배치 처리용)
	List<CrawledNotice> findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();

	// 성격이 미분류로 남은 크롤링 게시글 조회 (백필용)
	// 수정 시각이 갱신되지 않도록 Post 엔티티 대신 투영으로 조회하고, id 커서로 순회한다.
	@Query("""
		    SELECT new net.causw.app.main.domain.integration.crawled.repository.query.PostCategoryBackfillTarget(
				n.id, p.id, n.title)
		    FROM CrawledNotice n
		    JOIN n.post p
		    WHERE p.category IS NULL
		    AND p.isDeleted = false
		    AND n.id > :cursor
		    ORDER BY n.id
		""")
	List<PostCategoryBackfillTarget> findBackfillTargets(@Param("cursor") String cursor, Pageable pageable);
}
