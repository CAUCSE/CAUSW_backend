package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPostSubscribeRepository extends JpaRepository<UserPostSubscribe, String> {
	@EntityGraph(attributePaths = {"user"})
	List<UserPostSubscribe> findByPostAndIsSubscribedTrue(Post post);

	Optional<UserPostSubscribe> findByUserAndPost(User user, Post post);

}
