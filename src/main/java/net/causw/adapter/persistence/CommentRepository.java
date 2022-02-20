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

    @Query(value = "select * from tb_comment as co " +
            "join tb_post as p on co.post_id = p.id " +
            "join tb_board as b on p.board_id = b.id " +
            "left join tb_circle as c on c.id = b.circle_id " +
            "left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id " +
            "where co.user_id = :user_id and p.is_deleted = false and b.is_deleted = false and  co.is_deleted = false " +
            "and (c is null or (c.is_deleted = false and cm.status = 'MEMBER')) ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Comment> findByUserId(@Param("user_id") String userId, Pageable pageable);
}
