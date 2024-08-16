package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, String> {

}
