package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.LikeChildComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildCommentLikeRepository extends JpaRepository<LikeChildComment, Long> {

}
