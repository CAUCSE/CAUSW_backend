package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.v2.event.PostLikedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostNotificationHandler {

	private final LikePostReader likePostReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;
	private final UserBlockEntityService userBlockEntityService;

	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(PostLikedEvent event) {
		Post post = event.post();
		User liker = event.liker();
		User postWriter = post.getWriter();

		if (liker.getId().equals(postWriter.getId())) {
			return;
		}

		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(postWriter.getId());
		if (!settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST)) {
			return;
		}

		if (userBlockEntityService.existsBlockByUsers(liker, postWriter)) {
			return;
		}

		long likeCount = likePostReader.countByPostId(post.getId());
		if (!isLikeCountMilestone(likeCount)) {
			return;
		}

		String title = "내 글에 좋아요";
		String body = String.format("게시글에 좋아요가 %d개 달렸습니다.", likeCount);

		Notification notification = notificationWriter.save(
			Notification.of(liker, title, body, NoticeType.POST, post.getId(), post.getBoard().getId()));

		notificationPushSender.sendToUser(postWriter, title, body);
		notificationWriter.saveLog(postWriter, notification);
	}

	private boolean isLikeCountMilestone(long count) {
		if (count == 5 || count == 10 || count == 50 || count == 100 || count == 500) return true;
		return count >= 1000 && count % 1000 == 0;
	}
}
