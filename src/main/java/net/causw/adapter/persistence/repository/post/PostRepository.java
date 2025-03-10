package net.causw.adapter.persistence.repository.post;

import net.causw.adapter.persistence.form.Form;
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

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
    Long countCommentsByPostId(@Param("postId") String postId);

    @Query("SELECT COUNT(cc) FROM ChildComment cc WHERE cc.parentComment.post.id = :postId AND cc.isDeleted = false")
    Long countChildCommentsByPostId(@Param("postId") String postId);

    // 게시글에 작성된 모든댓글(댓글 + 대댓글)의 수 반환
    default Long countAllCommentByPost_Id(String postId) {
        Long commentCount = countCommentsByPostId(postId);
        Long childCommentCount = countChildCommentsByPostId(postId);
        return commentCount + childCommentCount;
    }
    Optional<Post> findByIdAndIsDeletedFalse(String postId);

    Optional<Post> findByForm(Form form);
}
