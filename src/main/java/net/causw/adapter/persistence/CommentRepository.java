package net.causw.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByPost_IdOrderByCreatedAtDesc(String postId, Pageable pageable);

    Long countByPost_IdAndIsDeletedIsFalse(String postId);

    @Query(value = "SELECT * " +
            "FROM TB_COMMENT AS c " +
            "WHERE c.user_id = :user_id AND c.is_deleted = false ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Comment> findByUserId(@Param("user_id") String userId, Pageable pageable);
}
