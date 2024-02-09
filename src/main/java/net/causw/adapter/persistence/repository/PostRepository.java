package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findAllByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId, Pageable pageable);
    Page<Post> findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(String boardId, Pageable pageable, boolean IsDeleted);
    Page<Post> findAllByBoard_IdOrderByCreatedAtDesc(String boardId, Pageable pageable);
    Optional<Post> findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId);


    //해당 동아리의 동아리장, 관리자, 학생회장인 경우 삭제여부와 관계없이 모든 게시글 검색
    @Query(value = "SELECT * " +
            "FROM tb_post AS p " +
            "WHERE p.title LIKE CONCAT('%', :title, '%')AND p.board_id = :boardId ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> searchByTitle(@Param("title") String title, @Param("boardId") String boardId, Pageable pageable);

    //해당 동아리의 동아리장, 관리자, 학생회장이 아닌경우 삭제되지 않은 게시글 검색
    @Query(value = "SELECT * " +
            "FROM tb_post AS p " +
            "WHERE p.title LIKE CONCAT('%', :title, '%')AND p.board_id = :boardId AND p.is_deleted = :isDeleted ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> searchByTitle(@Param("title") String title, @Param("boardId") String boardId, Pageable pageable, boolean isDeleted);


    @Query(value = "select * from tb_post as p " +
            "join tb_board as b on p.board_id = b.id " +
            "left join tb_circle as c on c.id = b.circle_id " +
            "left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id " +
            "where p.user_id = :user_id and p.is_deleted = false and b.is_deleted = false " +
            "and (c is null or (cm.status = 'MEMBER' and c.is_deleted = false)) ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findByUserId(@Param("user_id") String userId, Pageable pageable);
}
