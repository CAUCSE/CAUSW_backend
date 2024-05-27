package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.comment.ChildComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildCommentRepository extends JpaRepository<ChildComment, String> {
    Page<ChildComment> findByParentComment_IdOrderByCreatedAtAsc(String parentCommentId, Pageable pageable);

    Long countByParentComment_IdAndIsDeletedIsFalse(String parentCommentId);

    @Query("select c from ChildComment c where c.parentComment.id = :parentCommentId")
    List<ChildComment> findByParentComment_Id(@Param("parentCommentId") String parentCommentId);

}
