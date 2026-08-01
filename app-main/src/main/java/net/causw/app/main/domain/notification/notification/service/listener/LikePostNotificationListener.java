package net.causw.app.main.domain.notification.notification.service.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostNotificationListener {

	private final PostLikeMilestoneAchievementReader achievementReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;
	private final BlockReader blockReader;

	/**
	 * 좋아요 마일스톤 알림 처리 핸들러
	 * - 게시글 작성자에게 좋아요 알림을 보내는 역할
	 * - 커밋된 마일스톤 이력에 기록된 좋아요 수로 알림을 생성함
	 * - 작성자가 좋아요를 누른 경우, 또는 작성자가 좋아요 알림을 꺼놓은 경우, 또는 작성자가 좋아요 누른 유저를 차단한 경우에는 알림을 보내지 않음
	 * - 알림 방향: liker -> postWriter
	 * @param event 게시글 좋아요 마일스톤 알림 이벤트 객체
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(PostLikeMilestoneNotificationEvent event) {
		PostLikeMilestoneAchievement achievement = achievementReader.findById(event.achievementId());
		Post post = achievement.getPost();
		User liker = achievement.getTriggerUser();
		User postWriter = post.getWriter();

		// 작성자가 좋아요를 누른 경우 알림을 보내지 않음
		if (liker.getId().equals(postWriter.getId())) {
			return;
		}

		// 작성자가 좋아요 알림을 꺼놓은 경우 알림을 보내지 않음
		UserNotificationSettingMap settingMap = notificationSettingReader.findSettingMap(postWriter.getId());
		if (!settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST)) {
			return;
		}

		// 작성자가 좋아요 누른 유저를 차단한 경우 알림을 보내지 않음
		if (blockReader.existsByBlockerAndBlocked(postWriter, liker)) {
			return;
		}

		long likeCount = achievement.getMilestoneCount();

		// 게시글 좋아요 알림 생성
		String serviceTitle = String.format("게시물이 좋아요 %d개를 달성했습니다!", likeCount);
		String serviceBody = String.format("내 게시글에 좋아요가 %d개 달렸어요.", likeCount);
		String pushTitle = String.format("게시물 좋아요 %d개 달성", likeCount);
		PushNotificationData pushData = new PushNotificationData(NoticeType.COMMUNITY, post.getId(),
			post.getBoard().getId());

		// 알림 발송자를 게시글 작성자로 설정하여 알림 생성
		Notification notification = notificationWriter.save(
			Notification.of(postWriter, serviceTitle, serviceBody, NoticeType.COMMUNITY, post.getId(),
				post.getBoard().getId()));

		// 작성자에게 푸시 알림 발송 및 알림 로그 저장
		notificationPushSender.sendToUser(postWriter, pushTitle, serviceBody, pushData);
		notificationWriter.saveLog(postWriter, notification);
	}
}
