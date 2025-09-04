package net.causw.app.main.repository.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.comment.LikeComment;

public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
	Boolean existsByCommentIdAndUserId(String commentId, String UserId);

	Long countByCommentId(String commentId);

	void deleteLikeByCommentIdAndUserId(String commentId, String userId);
}
