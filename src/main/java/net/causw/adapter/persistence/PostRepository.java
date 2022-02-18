package net.causw.adapter.persistence;

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
    Optional<Post> findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(String boardId);

    @Query(value = "SELECT * " +
            "FROM TB_POST AS p " +
            "WHERE p.title = %:title% ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> searchByTitle(@Param("title") String userName, Pageable pageable);
    @Query(value = "SELECT * " +
            "FROM TB_POST AS p " +
            "LEFT JOIN TB_USER AS u ON p.user_id = u.id " +
            "WHERE u.name = %:user_name% ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> searchByWriter(@Param("user_name") String userName, Pageable pageable);

    @Query(value = "select * from tb_post as p " +
            "join tb_board as b on p.board_id = b.id " +
            "left join tb_circle as c on c.id = b.circle_id " +
            "left join tb_circle_member as cm on p.user_id = cm.user_id and c.id = cm.circle_id " +
            "where p.user_id := user_id and p.is_deleted = false and b.is_deleted = false " +
            "and (c is null or (cm.status = 'MEMBER' and c.is_deleted = false)) ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findByUserId(@Param("user_id") String userId, Pageable pageable);
}
