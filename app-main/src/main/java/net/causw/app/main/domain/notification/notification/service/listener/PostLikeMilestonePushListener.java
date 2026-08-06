package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.notification.notification.event.PostLikeMilestonePushEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeMilestonePushListener {

	private final UserReader userReader;
	private final NotificationPushSender notificationPushSender;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(PostLikeMilestonePushEvent event) {
		User recipient = userReader.findUserById(event.recipientUserId());
		notificationPushSender.sendToUser(
			recipient,
			event.pushTitle(),
			event.pushBody(),
			event.pushData());
	}
}
