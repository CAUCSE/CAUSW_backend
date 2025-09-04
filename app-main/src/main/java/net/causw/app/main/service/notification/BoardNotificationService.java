package net.causw.app.main.service.notification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.Notification;
import net.causw.app.main.domain.model.entity.notification.NotificationLog;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.notification.NoticeType;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.dto.notification.BoardNotificationDto;
import net.causw.app.main.infrastructure.firebase.FcmUtils;
import net.causw.app.main.repository.notification.NotificationLogRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.notification.UserBoardSubscribeRepository;
import net.causw.app.main.service.userBlock.UserBlockEntityService;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardNotificationService implements NotificationService {
	private final FirebasePushNotificationService firebasePushNotificationService;
	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final UserBoardSubscribeRepository userBoardSubscribeRepository;
	private final FcmUtils fcmUtils;
	private final UserBlockEntityService userBlockEntityService;

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
	public void sendByBoardIsSubscribed(Board board, Post post) {
		// 1. 게시글 작성자 추출
		User writer = post.getWriter();
		// 2. 게시글 작성자를 차단한 유저 추출
		Set<String> blockerUserIdsByBlockee = userBlockEntityService.findBlockerUserIdsByBlockee(writer);
		// 3. 게시글 작성자를 차단한 유저를 제외한 구독목록 가져오기
		List<UserBoardSubscribe> userBoardSubscribes = userBoardSubscribeRepository
			.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(board, blockerUserIdsByBlockee);

		List<UserBoardSubscribe> userBoardSubscribeList = userBoardSubscribes
			.stream()
			.filter(subscribe -> board.getIsAlumni() // 동문회 허용 게시판인 경우 졸업생에게 게시판 알림
				|| subscribe.getUser().getAcademicStatus() != AcademicStatus.GRADUATED
			).toList();

		BoardNotificationDto boardNotificationDto = BoardNotificationDto.of(board, post);

		Notification notification = Notification.of(post.getWriter(), boardNotificationDto.getTitle(),
			boardNotificationDto.getBody(), NoticeType.BOARD, post.getId(), board.getId());

		saveNotification(notification);

		userBoardSubscribeList.stream()
			.map(UserBoardSubscribe::getUser)
			.forEach(user -> {
					fcmUtils.cleanInvalidFcmTokens(user);
					Set<String> copy = new HashSet<>(user.getFcmTokens());
					copy.forEach(
						token -> send(user, token, boardNotificationDto.getTitle(), boardNotificationDto.getBody()));
					saveNotificationLog(user, notification);
				}
			);
	}
}
