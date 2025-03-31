package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.UserPostSubscribe;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPostSubscribeRepository extends JpaRepository<UserPostSubscribe ,String> {
    @EntityGraph(attributePaths = {"user"})
    List<UserPostSubscribe> findByPostAndIsSubscribedTrue(Post post);

    Optional<UserPostSubscribe> findByUserAndPost(User user, Post post);

}
