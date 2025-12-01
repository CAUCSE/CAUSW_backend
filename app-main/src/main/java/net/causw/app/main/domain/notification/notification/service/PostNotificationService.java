package net.causw.app.main.domain.notification.notification.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.api.dto.notification.PostNotificationDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.entity.UserPostSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.notification.notification.repository.UserPostSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.UserBlockEntityService;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.infra.firebase.FcmUtils;

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
	private final FcmUtils fcmUtils;
	private final UserBlockEntityService userBlockEntityService;

	@Override
	public void send(User user, String targetToken, String title, String body) {
		try {
			firebasePushNotificationService.sendNotification(targetToken, title, body);
		} catch (FirebaseMessagingException e) {
			log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());
			fcmUtils.removeFcmToken(user, targetToken);
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
		User postWriter = post.getWriter();
		User commentWriter = comment.getWriter();
		Set<String> blockerUserIds = getBlockerUserIds(postWriter, commentWriter);
		List<UserPostSubscribe> userPostSubscribeList = userPostSubscribeRepository.findByPostAndIsSubscribedTrueExcludingBlockers(
			post, blockerUserIds);

		PostNotificationDto postNotificationDto = PostNotificationDto.of(post, comment);

		Notification notification = Notification.of(commentWriter, postNotificationDto.getTitle(),
			postNotificationDto.getBody(), NoticeType.POST, post.getId(), post.getBoard().getId());

		saveNotification(notification);

		userPostSubscribeList.stream()
			.map(UserPostSubscribe::getUser)
			.forEach(user -> {
				fcmUtils.cleanInvalidFcmTokens(user);
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				copy.forEach(token -> send(user, token, postNotificationDto.getTitle(), postNotificationDto.getBody()));
				saveNotificationLog(user, notification);
			});
	}

	private Set<String> getBlockerUserIds(User postWriter, User commentWriter) {
		Set<String> blockeeUserIds =
			Stream.of(postWriter, commentWriter)
				.filter(Objects::nonNull)
				.map(BaseEntity::getId)
				.collect(Collectors.toSet());

		return userBlockEntityService.findBlockerUserIdsByUserIds(blockeeUserIds);
	}
}
