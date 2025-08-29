package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserCommentSubscribeRepository extends JpaRepository<UserCommentSubscribe,String> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
   SELECT ucs FROM UserCommentSubscribe ucs
   WHERE ucs.comment = :comment AND ucs.isSubscribed = true
   AND (:#{#blockerUserIds.size()} = 0 OR ucs.user.id NOT IN :blockerUserIds)
   """)
    List<UserCommentSubscribe> findByCommentAndIsSubscribedTrue(
        @Param("comment") Comment comment,
        @Param("blockerUserIds") Set<String> blockerUserIds
    );

    Optional<UserCommentSubscribe> findByUserAndComment(User user, Comment comment);
}
