package net.causw.app.main.service.notification;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.notification.NoticeType;
import net.causw.app.main.dto.notification.CommentNotificationDto;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserCommentSubscribeRepository;
import net.causw.app.main.service.userBlock.UserBlockEntityService;

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
	public void sendByCommentIsSubscribed(Comment comment, ChildComment childComment) {
		User commentWriter = comment.getWriter();
		User childCommentWriter = childComment.getWriter();

		Set<String> blockerUserIds = getBlockerUserIds(commentWriter, childCommentWriter);

		List<UserCommentSubscribe> userCommentSubscribeList = userCommentSubscribeRepository.findByCommentAndIsSubscribedTrueExcludingBlockerUsers(
			comment, blockerUserIds);
		CommentNotificationDto commentNotificationDto = CommentNotificationDto.of(comment, childComment);

		Notification notification = Notification.of(childCommentWriter, commentNotificationDto.getTitle(),
			commentNotificationDto.getBody(), NoticeType.COMMENT, comment.getPost().getId(),
			comment.getPost().getBoard().getId());

		saveNotification(notification);

		userCommentSubscribeList.stream()
			.map(UserCommentSubscribe::getUser)
			.forEach(user -> {
				fcmUtils.cleanInvalidFcmTokens(user);
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				copy.forEach(
					token -> send(user, token, commentNotificationDto.getTitle(), commentNotificationDto.getBody()));
				saveNotificationLog(user, notification);
			});
	}

	/**
	 * 해당 댓글들에 대해 차단을 진행한 user의 id 가져오는 로직
	 *
	 * @param commentWriter 댓글 작성자
	 * @param childCommentWriter 대댓글 작성자
	 * @return 차단한 유저 ids Set
	 */
	private Set<String> getBlockerUserIds(User commentWriter, User childCommentWriter) {
		Set<String> blockeeUserIds =
			Stream.of(commentWriter, childCommentWriter)
				.filter(Objects::nonNull)
				.map(BaseEntity::getId)
				.collect(Collectors.toSet());

		return userBlockEntityService.findBlockerUserIdsByUserIds(blockeeUserIds);
	}
}
