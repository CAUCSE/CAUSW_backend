package net.causw.app.main.service.notification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.notification.NoticeType;
import net.causw.app.main.dto.notification.CommentNotificationDto;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserCommentSubscribeRepository;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentNotificationService implements NotificationService {
	private final FirebasePushNotificationService firebasePushNotificationService;
	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final UserCommentSubscribeRepository userCommentSubscribeRepository;

	@Override
	public void send(User user, String targetToken, String title, String body) {
		try {
			firebasePushNotificationService.sendNotification(targetToken, title, body);
		} catch (FirebaseMessagingException e) {
			log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());
			user.getFcmTokens().remove(targetToken);
			log.info("오류 발생으로 FCM 토큰 제거됨: {}", targetToken);
		} catch (Exception e) {
			log.error("FCM 전송 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
		}
	}

	@Override
	public void saveNotification(Notification notification) {
		notificationRepository.save(notification);
	}

	@Override
	public void saveNotificationLog(User user, Notification notification) {
		notificationLogRepository.save(NotificationLog.of(user, notification));
	}

	@Async("asyncExecutor")
	@Transactional
	public void sendByCommentIsSubscribed(Comment comment, ChildComment childComment) {
		List<UserCommentSubscribe> userCommentSubscribeList = userCommentSubscribeRepository.findByCommentAndIsSubscribedTrue(
			comment);
		CommentNotificationDto commentNotificationDto = CommentNotificationDto.of(comment, childComment);

		Notification notification = Notification.of(childComment.getWriter(), commentNotificationDto.getTitle(),
			commentNotificationDto.getBody(), NoticeType.COMMENT, comment.getPost().getId(),
			comment.getPost().getBoard().getId());

		saveNotification(notification);

		userCommentSubscribeList.stream()
			.map(UserCommentSubscribe::getUser)
			.forEach(user -> {
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				copy.forEach(
					token -> send(user, token, commentNotificationDto.getTitle(), commentNotificationDto.getBody()));
				saveNotificationLog(user, notification);
			});
	}
}
