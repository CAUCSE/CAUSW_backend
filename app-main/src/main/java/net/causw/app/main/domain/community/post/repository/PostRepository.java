package net.causw.app.main.domain.community.post.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.repository.query.PostCommentCount;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

	Optional<Post> findTop1ByBoard_IdAndIsDeletedIsFalseAndIsHiddenFalseOrderByCreatedAtDesc(String boardId);

	@EntityGraph(attributePaths = {"writer"})
	List<Post> findAllByBoard_IdAndIsDeletedIsFalseAndIsHiddenFalseOrderByCreatedAtDesc(String boardId);

	// fetch join으로 Board까지 가져오기
	@Query(value = "SELECT DISTINCT p FROM Post p JOIN FETCH p.board WHERE p.id = :id")
	Optional<Post> findById(@Param("id") String id);

	@Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.board WHERE p.id IN :ids")
	List<Post> findAllByIdInWithBoard(@Param("ids") Collection<String> ids);

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
	Long countCommentsByPostId(@Param("postId") String postId);

	@Query("""
		SELECT new net.causw.app.main.domain.community.post.repository.query.PostCommentCount(c.post.id, COUNT(c))
		FROM Comment c
		WHERE c.post.id IN :postIds AND c.isDeleted = false
		GROUP BY c.post.id
		""")
	List<PostCommentCount> countCommentsByPostIds(@Param("postIds") Collection<String> postIds);

	// 게시판 삭제 시, 게시글도 함께 삭제
	@Query("UPDATE Post p SET p.isDeleted = true " +
		"WHERE p.board.id = :boardId AND p.isDeleted = false")
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	int deleteAllPostsByBoardId(@Param("boardId") String boardId);

	Optional<Post> findByIdAndIsDeletedFalseAndIsHiddenFalse(String postId);

	@EntityGraph(attributePaths = {"writer", "board"})
	@Query("""
		SELECT p FROM Post p
		WHERE (:boardId IS NULL OR p.board.id = :boardId)
		AND (:category IS NULL OR p.category = :category)
		AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted)
		AND (:isHidden IS NULL OR p.isHidden = :isHidden)
		AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%')
			OR p.content LIKE CONCAT('%', :keyword, '%'))
		AND (:writerKeyword IS NULL OR :writerKeyword = ''
			OR p.writer.name LIKE CONCAT('%', :writerKeyword, '%')
			OR p.writer.nickname LIKE CONCAT('%', :writerKeyword, '%'))
		""")
	Page<Post> findAllForAdmin(
		@Param("boardId") String boardId,
		@Param("category") PostCategory category,
		@Param("isDeleted") Boolean isDeleted,
		@Param("isHidden") Boolean isHidden,
		@Param("keyword") String keyword,
		@Param("writerKeyword") String writerKeyword,
		Pageable pageable);

	// 성격 일괄 지정 (벌크 연산이라 updated_at이 갱신되지 않음)
	// 대상 조회와 갱신 사이에 수동 지정되거나 삭제된 게시글을 덮어쓰지 않도록 현재 상태를 다시 확인한다.
	@Modifying
	@Query("""
			UPDATE Post p
			SET p.category = :category
			WHERE p.id IN :postIds
			AND p.category IS NULL
			AND p.isCrawled = true
			AND p.isDeleted = false
		""")
	int updateCategoryByIds(
		@Param("category") PostCategory category,
		@Param("postIds") Collection<String> postIds);

	// 성격이 미분류인 크롤링 게시글 조회 (관리자 수동 분류용)
	@Query("""
			SELECT p FROM Post p
			JOIN FETCH p.board
			WHERE p.category IS NULL
			AND p.isCrawled = true
			AND p.isDeleted = false
			ORDER BY p.createdAt ASC, p.id ASC
		""")
	Page<Post> findUncategorizedCrawledPosts(Pageable pageable);

	@Modifying(clearAutomatically = true)
	@Query("""
			update Post p
			   set p.viewCount = p.viewCount + 1
			 where p.id = :postId
			   and p.isDeleted = false
		""")
	void incrementViewCount(@Param("postId") String postId);
}
