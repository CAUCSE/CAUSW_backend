package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.post.FavoritePost;
import net.causw.adapter.persistence.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
}
