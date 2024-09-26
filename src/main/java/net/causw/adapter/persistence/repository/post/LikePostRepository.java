package net.causw.adapter.persistence.repository.post;

import net.causw.adapter.persistence.post.LikePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikePostRepository extends JpaRepository<LikePost, String> {
    Boolean existsByPostIdAndUserId(String postId, String userId);

    Long countByPostId(String postId);
}
