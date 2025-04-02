package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.UserCommentSubscribe;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentSubscribeResponseDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCommentSubscribeRepository extends JpaRepository<UserCommentSubscribe ,String> {

    @EntityGraph(attributePaths = {"user"})
    List<UserCommentSubscribe> findByCommentAndIsSubscribedTrue(Comment comment);

    Optional<UserCommentSubscribe> findByUserAndComment(User user, Comment comment);
}
