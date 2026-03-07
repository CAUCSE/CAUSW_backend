package net.causw.app.main.domain.notification.notification.service.v1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.api.v1.dto.BoardNotificationDto;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.infra.firebase.FcmUtils;

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

	/**
	 * 게시판 구독자들에게 새 게시글 알림 전송 (비동기 처리)
	 * <p>
	 * 이 메서드는 asyncExecutor 스레드 풀에서 비동기로 실행됩니다.
	 * 호출 스레드는 즉시 반환되고, 알림 전송은 별도 스레드에서 처리됩니다.
	 * <p>
	 * 처리 과정:
	 * <ol>
	 * <li>게시글 작성자를 차단한 유저 필터링</li>
	 * <li>게시판 구독자 중 알림 수신 대상자 필터링 (졸업생 제외 등)</li>
	 * <li>알림 생성 및 저장</li>
	 * <li>각 구독자에게 FCM 푸시 알림 전송 및 로그 저장</li>
	 * </ol>
	 *
	 * @param board 게시글이 작성된 게시판
	 * @param post 작성된 게시글
	 */
	@Async("asyncExecutor")
	@Transactional
	public void sendByBoardIsSubscribed(Board board, Post post) {
		// 1. 게시글 작성자 추출
		User writer = post.getWriter();

		// 2. 게시글 작성자를 차단한 유저 ID 목록 추출
		// 차단한 유저에게는 알림을 보내지 않기 위함
		Set<String> blockerUserIdsByBlockee = userBlockEntityService.findBlockerUserIdsByBlockee(writer);

		// 3. 게시판을 구독 중이고, 작성자를 차단하지 않은 유저 목록 가져오기
		List<UserBoardSubscribe> userBoardSubscribes = userBoardSubscribeRepository
			.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(board, blockerUserIdsByBlockee);

		// 4. 알림 수신 대상자 필터링
		// - 동문회 허용 게시판이면 졸업생도 포함
		// - 일반 게시판이면 졸업생 제외
		List<UserBoardSubscribe> userBoardSubscribeList = userBoardSubscribes
			.stream()
			.filter(subscribe -> board.getIsAlumni() // 동문회 허용 게시판인 경우 졸업생에게 게시판 알림
				|| subscribe.getUser().getAcademicStatus() != AcademicStatus.GRADUATED)
			.toList();

		// 5. 알림 DTO 생성 (제목, 본문 포함)
		BoardNotificationDto boardNotificationDto = BoardNotificationDto.of(board, post);

		// 6. 알림 엔티티 생성 (작성자, 제목, 본문, 알림 타입, 게시글 ID, 게시판 ID)
		Notification notification = Notification.of(post.getWriter(), boardNotificationDto.getTitle(),
			boardNotificationDto.getBody(), NoticeType.BOARD, post.getId(), board.getId());

		// 7. 알림 저장
		saveNotification(notification);

		// 8. 각 구독자에게 푸시 알림 전송
		userBoardSubscribeList.stream()
			.map(UserBoardSubscribe::getUser)
			.forEach(user -> {
				// 유효하지 않은 FCM 토큰 정리
				fcmUtils.cleanInvalidFcmTokens(user);
				// FCM 토큰 복사 (동시성 문제 방지)
				Set<String> copy = new HashSet<>(user.getFcmTokens());
				// 각 토큰으로 푸시 알림 전송
				copy.forEach(
					token -> send(user, token, boardNotificationDto.getTitle(), boardNotificationDto.getBody()));
				// 사용자별 알림 로그 저장
				saveNotificationLog(user, notification);
			});
	}
}
