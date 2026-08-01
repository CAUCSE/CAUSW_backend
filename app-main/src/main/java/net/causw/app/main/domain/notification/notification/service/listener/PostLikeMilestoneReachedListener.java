package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneReachedEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementRecorder;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneNotificationProcessor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeMilestoneReachedListener {

	private final PostLikeMilestoneAchievementRecorder achievementRecorder;
	private final PostLikeMilestoneNotificationProcessor notificationProcessor;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PostLikeMilestoneReachedEvent event) {
		achievementRecorder.record(event)
			.ifPresent(notificationProcessor::process);
	}
}
