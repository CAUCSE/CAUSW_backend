package net.causw.app.main.domain.community.comment.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.query.PostCommentCount;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
	Long countByPostId(@Param("postId") String postId);

	@Query("""
		SELECT new net.causw.app.main.domain.community.comment.repository.query.PostCommentCount(c.post.id, COUNT(c))
		FROM Comment c
		WHERE c.post.id IN :postIds AND c.isDeleted = false
		GROUP BY c.post.id
		""")
	List<PostCommentCount> countByPostIds(@Param("postIds") Collection<String> postIds);
}
