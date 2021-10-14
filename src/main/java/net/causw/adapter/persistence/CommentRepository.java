package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    // TODO : GHJANG : Pagination & Ordering
    @Query(value = "SELECT * from TB_COMMENT where TB_COMMENT.post_id = ?1 and TB_COMMENT.parent_comment_id is null", nativeQuery = true)
    List<Comment> findByPostId(String postId);

    Long countByPost_Id(String postId);
}
