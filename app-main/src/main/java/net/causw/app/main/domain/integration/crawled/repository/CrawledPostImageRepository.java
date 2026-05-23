package net.causw.app.main.domain.integration.crawled.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;

@Repository
public interface CrawledPostImageRepository extends JpaRepository<CrawledPostImage, String> {

	/**
	 * postId 목록에 해당하는 크롤링 이미지 엔티티 조회
	 */
	List<CrawledPostImage> findAllByPostIdInOrderByPostIdAscImageOrderAsc(List<String> postIds);

	void deleteAllByPostId(String postId);
}
