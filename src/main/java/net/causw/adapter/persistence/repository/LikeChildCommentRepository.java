package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.LikeChildComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeChildCommentRepository extends JpaRepository<LikeChildComment, Long> {
    boolean existsByChildCommentIdAndUserId(String childCommentId, String userId);

    Long countByChildCommentId(String childCommentId);

}
