package net.causw.app.main.repository.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.comment.LikeChildComment;

public interface LikeChildCommentRepository extends JpaRepository<LikeChildComment, Long> {
	Boolean existsByChildCommentIdAndUserId(String childCommentId, String userId);

	Long countByChildCommentId(String childCommentId);

	void deleteLikeByChildCommentIdAndUserId(String childCommentId, String userId);

}
