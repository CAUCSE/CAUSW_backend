package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.UserPostSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserPostSubscribeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPostSubscribeReader {

	private final UserPostSubscribeRepository userPostSubscribeRepository;

	public List<UserPostSubscribe> findForNotification(Post post, Set<String> blockerUserIds) {
		return userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(post, blockerUserIds);
	}
}
