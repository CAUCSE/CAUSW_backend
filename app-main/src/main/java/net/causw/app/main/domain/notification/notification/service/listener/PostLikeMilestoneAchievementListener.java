package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneNotificationEvent;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeMilestoneAchievementListener {

	private final PostReader postReader;
	private final UserReader userReader;
	private final PostLikeMilestoneAchievementWriter achievementWriter;
	private final ApplicationEventPublisher eventPublisher;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(PostLikeMilestoneReachedEvent event) {
		Post post = postReader.findById(event.postId());
		User liker = userReader.findUserById(event.likerId());

		achievementWriter.savePendingIfAbsent(post, liker, event.milestoneCount())
			.ifPresent(achievement -> eventPublisher.publishEvent(
				new PostLikeMilestoneNotificationEvent(achievement.getId())));
	}
}
