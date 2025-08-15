package net.causw.app.main.repository.comment;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.writer w " +
            "WHERE c.post.id = :postId " +
            "ORDER BY c.createdAt")
    Page<Comment> findByPost_IdOrderByCreatedAt(@Param("postId") String postId, Pageable pageable);

    Boolean existsByPostIdAndIsDeletedFalse(String postId);

    @Query(value = "select * from tb_comment as co " +
            "join tb_post as p on co.post_id = p.id " +
            "join tb_board as b on p.board_id = b.id " +
            "left join tb_circle as c on c.id = b.circle_id " +
            "left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id " +
            "where co.user_id = :user_id and p.is_deleted = false and b.is_deleted = false and  co.is_deleted = false " +
            "and (c.id is null or (c.is_deleted = false and cm.status = 'MEMBER')) ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Comment> findByUserId(@Param("user_id") String userId, Pageable pageable);

    @Query("SELECT DISTINCT p " +
            "FROM Comment c " +
            "JOIN c.post p " +
            "WHERE c.writer.id = :userId AND c.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserId(@Param("userId") String userId, Pageable pageable);

}
