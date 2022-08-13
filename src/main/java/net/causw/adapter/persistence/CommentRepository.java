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

    @Query(value = "select co.id as id, co.created_at as created_at, co.updated_at as updated_as, co.content as content , co.is_deleted as is_deleted, co.post_id as post_id, co.user_id as user_id from tb_comment as co\n" +
            "            join tb_post as p on co.post_id = p.id\n" +
            "            join tb_board as b on p.board_id = b.id\n" +
            "            left join tb_circle as c on c.id = b.circle_id\n" +
            "            left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id\n" +
            "            where co.user_id = :user_id and p.is_deleted = false and b.is_deleted = false and co.is_deleted = false\n" +
            "            and (c is null or (c.is_deleted = false and cm.status = 'MEMBER'))\n" +
            "union all\n" +
            "select cc.id as id, cc.created_at as created_at, cc.updated_at as updated_at, cc.content as content, cc.is_deleted as is_deleted, co.post_id as post_id, cc.user_id as user_id from tb_child_comment as cc\n" +
            "            join tb_comment as co on cc.parent_comment_id = co.id\n" +
            "            join tb_post as p on co.post_id = p.id\n" +
            "            join tb_board as b on p.board_id = b.id\n" +
            "            left join tb_circle as c on c.id = b.circle_id\n" +
            "            left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id\n" +
            "            where cc.user_id = :user_id and p.is_deleted = false and b.is_deleted = false and cc.is_deleted = false\n" +
            "            and (c is null or (c.is_deleted = false and cm.status = 'MEMBER'))\n" +
            "ORDER BY created_at DESC\n",
            nativeQuery = true)
    Page<Comment> findByUserId(@Param("user_id") String userId, Pageable pageable);
}
