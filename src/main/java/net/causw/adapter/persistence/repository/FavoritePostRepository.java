package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.FavoritePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);

    Optional<FavoritePost> findByPostIdAndUserId(String postId, String userId);
}
