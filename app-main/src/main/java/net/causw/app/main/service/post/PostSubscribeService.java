package net.causw.app.main.service.post;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.repository.notification.UserPostSubscribeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostSubscribeService {
	private final UserPostSubscribeRepository userPostSubscribeRepository;

	public Boolean isPostSubscribed(User user, Post post){
		return userPostSubscribeRepository.findByUserAndPost(user, post)
			.map(UserPostSubscribe::getIsSubscribed)
			.orElse(false);
	}
}
