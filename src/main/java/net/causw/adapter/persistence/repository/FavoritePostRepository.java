package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.FavoritePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
}
