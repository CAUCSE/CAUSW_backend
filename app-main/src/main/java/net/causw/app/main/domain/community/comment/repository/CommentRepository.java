package net.causw.app.main.domain.community.comment.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

	@Query("SELECT c FROM Comment c " +
		"LEFT JOIN FETCH c.writer w " +
		"WHERE c.post.id = :postId " +
		"AND c.parentComment IS NULL " +
		"ORDER BY c.createdAt ASC")
	Page<Comment> findRootCommentsByPostId(@Param("postId") String postId, Pageable pageable);

	@Query("SELECT c FROM Comment c " +
		"LEFT JOIN FETCH c.writer w " +
		"LEFT JOIN FETCH c.parentComment pc " +
		"LEFT JOIN FETCH c.post p " +
		"WHERE c.parentComment.id IN :parentCommentIds " +
		"ORDER BY c.createdAt ASC")
	List<Comment> findRepliesByParentCommentIds(@Param("parentCommentIds") List<String> parentCommentIds);

	Optional<Comment> findByIdAndIsDeletedFalse(String commentId);

}
