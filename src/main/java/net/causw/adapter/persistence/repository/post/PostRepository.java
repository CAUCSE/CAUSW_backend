package net.causw.adapter.persistence.repository.post;

import net.causw.adapter.persistence.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId, Pageable pageable);
    Page<Post> findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(String boardId, Pageable pageable, boolean IsDeleted);
    Page<Post> findAllByBoard_IdOrderByCreatedAtDesc(String boardId, Pageable pageable);
    Optional<Post> findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId);
    List<Post> findTop3ByBoard_IdAndIsDeletedOrderByCreatedAtDesc(String boardId, Boolean isDeleted);

    //특정 게시판에서 삭제 여부와 관계없이 title 이 포함된 게시글 검색
    @Query(value = "SELECT * " +
            "FROM tb_post AS p " +
            "WHERE p.title LIKE CONCAT('%', :title, '%')AND p.board_id = :boardId ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findByTitleAndBoard_Id(@Param("title") String title, @Param("boardId") String boardId, Pageable pageable);

    //특정 게시판에서 삭제 여부를 고려하여 title 이 포함된 게시글 검색
    @Query(value = "SELECT * " +
            "FROM tb_post AS p " +
            "WHERE p.title LIKE CONCAT('%', :title, '%')AND p.board_id = :boardId AND p.is_deleted = :isDeleted ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findByTitleBoard_IdAndDeleted(@Param("title") String title, @Param("boardId") String boardId, Pageable pageable, @Param("isDeleted") boolean isDeleted);

    // 특정 사용자가 작성한 게시글 검색
    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN p.board b " +
            "LEFT JOIN b.circle c " +
            "LEFT JOIN CircleMember cm ON p.writer.id = cm.user.id AND c.id = cm.circle.id " +
            "WHERE p.writer.id = :user_id AND p.isDeleted = false AND b.isDeleted = false " +
            "AND (c.id IS NULL " +
            "OR (cm.status = 'MEMBER' AND c.isDeleted = false)) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByUserId(@Param("user_id") String userId, Pageable pageable);

    // fetch join으로 Board까지 가져오기
    @Query(value = "SELECT DISTINCT p FROM Post p JOIN FETCH p.board WHERE p.id = :id")
    Optional<Post> findById(@Param("id") String id);

    // 게시물에 작성된 모든 댓글(댓글+대댓글)의 수 세기
    @Query(value = "SELECT COUNT(DISTINCT c.id) + COUNT(DISTINCT cc.id) - COUNT(DISTINCT CASE WHEN c.is_deleted = true AND NOT cc.is_deleted IS NULL THEN c.id END)" +
            "FROM tb_post AS p " +
            "JOIN tb_comment AS c ON p.id = c.post_id " +
            "LEFT JOIN tb_child_comment AS cc ON c.id = cc.parent_comment_id " +
            "WHERE p.id = :postId AND p.is_deleted = false " +
            "AND NOT (c.is_deleted = true AND cc.is_deleted IS NULL)" +
            "AND (cc.is_deleted = false OR cc.is_deleted IS NULL)", nativeQuery = true)
    Long countAllCommentByPost_Id(@Param("postId") String postId);
}
