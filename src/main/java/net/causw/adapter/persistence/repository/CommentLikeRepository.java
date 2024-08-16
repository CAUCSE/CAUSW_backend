package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.LikeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<LikeComment, Long> {
}
