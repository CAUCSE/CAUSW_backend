package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserPostSubscribeRepository extends JpaRepository<UserPostSubscribe,String> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
   SELECT ups FROM UserPostSubscribe ups
   WHERE ups.post = :post AND ups.isSubscribed = true
   AND (:#{#blockerUserIds.size()} = 0 OR ups.user.id NOT IN :blockerUserIds)
   """)
    List<UserPostSubscribe> findByPostAndIsSubscribedTrue(
        @Param("post") Post post,
        @Param("blockerUserIds") Set<String> blockerUserIds
    );

    Optional<UserPostSubscribe> findByUserAndPost(User user, Post post);

}
