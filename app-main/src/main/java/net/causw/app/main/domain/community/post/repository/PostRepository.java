package net.causw.app.main.domain.community.post.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.domain.community.post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
	@EntityGraph(attributePaths = {"postAttachImageList"})
	Page<Post> findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId, Pageable pageable);

	Optional<Post> findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId);

	// Repository
	@Query("""
		    SELECT p FROM Post p
		    LEFT JOIN FETCH p.writer w
		    WHERE p.board.id = :boardId
		    AND (:includeDeleted = true OR p.isDeleted = false)
		    AND (:#{#blockedUserIds.size()} = 0 OR p.writer.id NOT IN :blockedUserIds)
		    AND (:keyword IS NULL OR :keyword = '' OR
				 p.title LIKE CONCAT('%', :keyword, '%') OR
		         p.content LIKE CONCAT('%', :keyword, '%') OR
				 p.writer.nickname LIKE CONCAT('%', :keyword, '%'))
		    ORDER BY p.createdAt DESC
		""")
	Page<Post> findPostsByBoardWithFilters(
		@Param("boardId") String boardId,
		@Param("includeDeleted") boolean includeDeleted,
		@Param("blockedUserIds") Set<String> blockedUserIds,
		@Param("keyword") String keyword,
		Pageable pageable);

	// fetch join으로 Board까지 가져오기
	@Query(value = "SELECT DISTINCT p FROM Post p JOIN FETCH p.board WHERE p.id = :id")
	Optional<Post> findById(@Param("id") String id);

	@Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.board WHERE p.id IN :ids")
	List<Post> findAllByIdInWithBoard(@Param("ids") Collection<String> ids);

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
	Long countCommentsByPostId(@Param("postId") String postId);

	// 게시글에 작성된 모든댓글의 수 반환
	default Long countAllCommentByPost_Id(String postId) {
		return countCommentsByPostId(postId);
	}

	Optional<Post> findByForm(Form form);

	//특정 게시판의 모든 게시글 조회 (해시 계산용)
	List<Post> findAllByBoardAndIsDeletedIsFalse(Board board);

	// 게시판 삭제 시, 게시글도 함께 삭제
	@Query("UPDATE Post p SET p.isDeleted = true " +
		"WHERE p.board.id = :boardId AND p.isDeleted = false")
	@Modifying
	int deleteAllPostsByBoardId(@Param("boardId") String boardId);

	Optional<Post> findByIdAndIsDeletedFalse(String postId);
}
