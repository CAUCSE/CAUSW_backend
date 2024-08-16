package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.ChildCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildCommentLikeRepository extends JpaRepository<ChildCommentLike, Long> {

}
