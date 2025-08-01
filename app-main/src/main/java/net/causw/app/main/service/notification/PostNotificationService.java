package net.causw.app.main.service.notification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.notification.NoticeType;
import net.causw.app.main.dto.notification.PostNotificationDto;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserPostSubscribeRepository;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostNotificationService implements NotificationService {
	private final FirebasePushNotificationService firebasePushNotificationService;
	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final UserPostSubscribeRepository userPostSubscribeRepository;

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
	public void sendByPostIsSubscribed(Post post, Comment comment) {
		List<UserPostSubscribe> userPostSubscribeList = userPostSubscribeRepository.findByPostAndIsSubscribedTrue(post);
		PostNotificationDto postNotificationDto = PostNotificationDto.of(post, comment);

		Notification notification = Notification.of(comment.getWriter(), postNotificationDto.getTitle(),
			postNotificationDto.getBody(), NoticeType.POST, post.getId(), post.getBoard().getId());

		saveNotification(notification);

		userPostSubscribeList.stream()
			.map(UserPostSubscribe::getUser)
			.forEach(user -> {
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				copy.forEach(token -> send(user, token, postNotificationDto.getTitle(), postNotificationDto.getBody()));
				saveNotificationLog(user, notification);
			});
	}

}
