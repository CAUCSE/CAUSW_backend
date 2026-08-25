package net.causw.app.main.domain.notification.notification.service.listener;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.SystemNoticeNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.util.NotificationTextUtil;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemNoticeNotificationListener {

	private static final String PUSH_TITLE = "시스템 공지";

	private final SystemNoticeReader systemNoticeReader;
	private final NotificationSettingReader notificationSettingReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;

	/**
	 * 시스템 공지 생성 알림 이벤트 핸들러.
	 * <p>
	 * 시스템 공지가 새로 생성되면, 전체 ACTIVE 유저 중 서비스 공지 알림 설정이 켜진 유저에게
	 * 푸시 알림 및 서비스 알림을 발송합니다. (수정 시에는 이 이벤트가 발행되지 않으므로 재알림하지 않습니다.)
	 * <ul>
	 *   <li>대상: 전체 ACTIVE 유저 중 서비스 공지 알림 설정 ON
	 *       ({@link UserNotificationSettingKey#SERVICE_NOTICE_ENABLED})</li>
	 * </ul>
	 *
	 * @param event 시스템 공지 생성 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(SystemNoticeNotificationEvent event) {
		// ID로 시스템 공지 게시글 조회
		Post post = systemNoticeReader.getSystemNoticePost(event.postId());
		User writer = post.getWriter();

		// 서비스 공지 알림 설정이 켜진 전체 ACTIVE 유저 조회
		List<User> targets = notificationSettingReader.findAllActiveUsersBySettingKey(
			UserNotificationSettingKey.SERVICE_NOTICE_ENABLED);

		// 푸시알림 제목: "시스템 공지" 고정
		// 푸시알림 내용/서비스 알림 제목: 공지 제목 (각각 최대 길이 초과 시 ...으로 표시)
		String serviceTitle = NotificationTextUtil.ellipsis(post.getTitle(),
			NotificationTextUtil.SERVICE_TITLE_MAX_LENGTH);
		String pushBody = NotificationTextUtil.ellipsis(post.getTitle(), NotificationTextUtil.PUSH_BODY_MAX_LENGTH);
		PushNotificationData pushData = new PushNotificationData(
			NoticeType.SYSTEM_NOTICE, post.getId(), post.getBoard().getId());

		// 알림 발송자를 게시글 작성자로 설정하여 알림 저장
		Notification notification = notificationWriter.save(
			Notification.of(writer, serviceTitle, pushBody, NoticeType.SYSTEM_NOTICE, post.getId(),
				post.getBoard().getId()));

		notificationPushSender.sendToUsers(targets, PUSH_TITLE, pushBody, pushData);
		notificationWriter.saveLogs(targets, notification);
	}
}
