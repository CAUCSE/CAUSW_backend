package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.LikePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<LikePost, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
}
