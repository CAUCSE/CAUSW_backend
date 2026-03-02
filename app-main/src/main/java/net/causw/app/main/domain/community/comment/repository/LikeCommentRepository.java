package net.causw.app.main.domain.community.comment.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.community.comment.entity.LikeComment;

public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
	Boolean existsByCommentIdAndUserId(String commentId, String UserId);

	Long countByCommentId(String commentId);

	void deleteLikeByCommentIdAndUserId(String commentId, String userId);

	@Query("SELECT l.comment.id " +
		"FROM LikeComment l " +
		"WHERE l.user.id = :userId " +
		"AND l.comment.id IN :commentIds")
	Set<String> findLikedCommentIdsByUserIdAndCommentIds(
		@Param("userId") String userId,
		@Param("commentIds") List<String> commentIds);
}
