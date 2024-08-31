package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.FavoritePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
    Boolean existsByPostIdAndUserId(String postId, String userId);

    Optional<FavoritePost> findByPostIdAndUserId(String postId, String userId);

    Page<FavoritePost> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    Long countByPostIdAndIsDeletedFalse(String postId);
}
