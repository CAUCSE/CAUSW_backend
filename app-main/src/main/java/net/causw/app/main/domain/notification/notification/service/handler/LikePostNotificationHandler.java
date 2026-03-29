package net.causw.app.main.domain.notification.notification.service.handler;

import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;
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
import net.causw.app.main.domain.notification.notification.event.PostLikedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostNotificationHandler {

	private final LikePostReader likePostReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;
	private final BlockReader blockReader;

	/**
	 * 좋아요 이벤트 처리 핸들러
	 * - 게시글 작성자에게 좋아요 알림을 보내는 역할
	 * - 좋아요 수가 특정 마일스톤(5, 10, 50, 100, 500, 1000의 배수)에 도달했을 때 알림을 보냄
	 * - 작성자가 좋아요를 누른 경우, 또는 작성자가 좋아요 알림을 꺼놓은 경우, 또는 작성자가 좋아요 누른 유저를 차단한 경우에는 알림을 보내지 않음
	 * - 알림 방향: liker -> postWriter
	 * @param event 게시글 좋아요 이벤트 객체
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handle(PostLikedEvent event) {
		Post post = event.post();
		User liker = event.liker();
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

		// 좋아요 수가 특정 마일스톤에 도달했는지 확인
		long likeCount = likePostReader.countByPostId(post.getId());
		if (!isLikeCountMilestone(likeCount)) {
			return;
		}

		// 게시글 좋아요 알림 생성
		Notification postLikeNotification = createPostLikeNotification(postWriter, likeCount, post.getId(), post.getBoard().getId());
		Notification notification = notificationWriter.save(postLikeNotification);

		// 작성자에게 푸시 알림 발송 및 알림 로그 저장
		notificationPushSender.sendToUser(postWriter, "내 게시글에 좋아요가 달렸습니다!", postLikeNotification.getBody());
		notificationWriter.saveLog(postWriter, notification);
	}

	/**
	 * 게시물 좋아요 알림 생성 메서드
	 * @param user 알림을 받을 유저
	 * @param postLikeCount 게시물이 달성한 좋아요 수
	 * @param postId 알림의 대상이 되는 게시물의 ID
	 * @param boardId 알림의 대상이 되는 게시물이 속한 게시판의 ID
	 * @return 게시물 좋아요 알림 Notification 객체
	 *
	 */
	private static Notification createPostLikeNotification(User user, long postLikeCount, String postId, String boardId) {
		String title = NoticeType.COMMUNITY.getTitle();
		String body = String.format("게시물이 좋아요 %d개를 달성했습니다!", postLikeCount);

		return Notification.of(user, title, body, NoticeType.COMMUNITY, postId, boardId);
	}

	/**
	 *
	 * @param count 좋아요 수가 특정 마일스톤(5, 10, 50, 100, 500, 1000의 배수)에 도달했는지 확인하는 메서드
	 * @return 좋아요 수가 마일스톤에 도달했으면 true, 그렇지 않으면 false
	 */
	private boolean isLikeCountMilestone(long count) {
		if (count == 5 || count == 10 || count == 50 || count == 100 || count == 500)
			return true;
		return count >= 1000 && count % 1000 == 0;
	}
}
