package net.causw.app.main.domain.community.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.comment.entity.LikeComment;

public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
	Boolean existsByCommentIdAndUserId(String commentId, String UserId);

	Long countByCommentId(String commentId);

	void deleteLikeByCommentIdAndUserId(String commentId, String userId);
}
