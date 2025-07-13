package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCommentSubscribeRepository extends JpaRepository<UserCommentSubscribe,String> {

    @EntityGraph(attributePaths = {"user"})
    List<UserCommentSubscribe> findByCommentAndIsSubscribedTrue(Comment comment);

    Optional<UserCommentSubscribe> findByUserAndComment(User user, Comment comment);
}
