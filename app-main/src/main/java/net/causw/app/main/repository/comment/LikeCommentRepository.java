package net.causw.app.main.repository.comment;

import net.causw.app.main.domain.model.entity.comment.LikeComment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
	Boolean existsByCommentIdAndUserId(String commentId, String UserId);

	Long countByCommentId(String commentId);

}
