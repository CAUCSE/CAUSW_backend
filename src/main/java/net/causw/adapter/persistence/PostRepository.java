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
}
