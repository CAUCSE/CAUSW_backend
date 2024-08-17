package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.LikePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikePostRepository extends JpaRepository<LikePost, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);

    Long countByPostId(String postId);
}
