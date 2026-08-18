package net.causw.app.main.domain.community.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;

@Repository
public interface CommentAnonymousNicknameRepository extends JpaRepository<CommentAnonymousNickname, String> {

	List<CommentAnonymousNickname> findAllByPostId(String postId);
}
