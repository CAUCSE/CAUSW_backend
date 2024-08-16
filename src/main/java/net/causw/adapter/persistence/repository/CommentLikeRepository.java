package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
}
