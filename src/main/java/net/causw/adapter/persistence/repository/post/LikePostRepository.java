package net.causw.adapter.persistence.repository.post;

import net.causw.adapter.persistence.post.LikePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikePostRepository extends JpaRepository<LikePost, String> {

    Boolean existsByPostIdAndUserId(String postId, String userId);

    void deleteLikeByPostIdAndUserId(String postId, String userId);

    Long countByPostId(String postId);

    @Query("SELECT lp " +
        "FROM LikePost lp " +
        "JOIN lp.post p " +
        "WHERE lp.user.id = :userId " +
        "ORDER BY p.createdAt DESC")
    Page<LikePost> findByUserId(@Param("userId") String userId, Pageable pageable);
}
