package net.causw.app.main.repository.post;

import net.causw.app.main.domain.model.entity.post.FavoritePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
    Boolean existsByPostIdAndUserId(String postId, String userId);

    Optional<FavoritePost> findByPostIdAndUserId(String postId, String userId);

    Long countByPostId(String postId);

    void deleteFavoriteByPostIdAndUserId(String postId, String userId);

    @Query("SELECT fp " +
            "FROM FavoritePost fp " +
            "JOIN fp.post p " +
            "WHERE fp.user.id = :userId " +
            "ORDER BY p.createdAt DESC")
    Page<FavoritePost> findByUserId(@Param("userId") String userId, Pageable pageable);
}
