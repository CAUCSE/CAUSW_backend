package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.ChildComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildCommentRepository extends JpaRepository<ChildComment, String> {
    Page<ChildComment> findByParentComment_IdOrderByCreatedAtAsc(String parentCommentId, Pageable pageable);

    Long countByParentComment_IdAndIsDeletedIsFalse(String parentCommentId);
}
