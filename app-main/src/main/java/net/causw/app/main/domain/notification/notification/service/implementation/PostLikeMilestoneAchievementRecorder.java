package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeMilestoneAchievementRecorder {

	private final PostReader postReader;
	private final UserReader userReader;
	private final PostLikeMilestoneAchievementWriter achievementWriter;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<String> record(PostLikeMilestoneReachedEvent event) {
		Post post = postReader.findById(event.postId());
		User liker = userReader.findUserById(event.likerId());

		return achievementWriter.savePendingIfAbsent(post, liker, event.milestoneCount())
			.map(achievement -> achievement.getId());
	}
}
