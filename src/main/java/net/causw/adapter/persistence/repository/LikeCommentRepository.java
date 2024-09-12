package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.LikeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCommentRepository extends JpaRepository<LikeComment, Long> {
    Boolean existsByCommentIdAndUserId(String commentId, String UserId);

    Long countByCommentId(String commentId);

}
