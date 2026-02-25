package net.causw.app.main.domain.notification.notification.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.notification.notification.service.v1.FirebasePushNotificationService;
import net.causw.app.main.domain.notification.notification.service.v1.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.notification.notification.repository.NotificationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.infra.firebase.FcmUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdmissionNotificationService implements NotificationService {
	private final FirebasePushNotificationService firebasePushNotificationService;
	private final NotificationRepository notificationRepository;
	private final NotificationLogRepository notificationLogRepository;
	private final UserRepository userRepository;
	private final FcmUtils fcmUtils;

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
	public void sendCreatedAdmissionToAdmins(String applicantUserId) {
		User applicant = userRepository.findById(applicantUserId).orElse(null);
		if (applicant == null) {
			return;
		}

		List<User> adminUsers = userRepository.findByRoleAndState(Role.ADMIN, UserState.ACTIVE);
		if (adminUsers.isEmpty()) {
			return;
		}

		String title = "서비스 알림";
		String body = applicant.getName() + " 사용자가 재학인증을 신청했어요.";

		Notification notification = Notification.of(
			applicant,
			title,
			body,
			NoticeType.ADMISSION,
			null,
			null);
		saveNotification(notification);

		adminUsers.forEach(admin -> {
			fcmUtils.cleanInvalidFcmTokens(admin);
			Set<String> copy = new HashSet<>(admin.getFcmTokens());
			copy.forEach(token -> send(admin, token, title, body));
			saveNotificationLog(admin, notification);
		});
	}

	@Async("asyncExecutor")
	@Transactional
	public void sendApprovedAdmissionToUser(String targetUserId, String adminUserId) {
		User targetUser = userRepository.findById(targetUserId).orElse(null);
		if (targetUser == null) {
			return;
		}

		User adminUser = userRepository.findById(adminUserId).orElse(null);
		String title = "서비스 알림";
		String body = "재학정보 인증이 완료되었어요";

		Notification notification = Notification.of(
			adminUser,
			title,
			body,
			NoticeType.ADMISSION,
			null,
			null);
		saveNotification(notification);

		fcmUtils.cleanInvalidFcmTokens(targetUser);
		Set<String> copy = new HashSet<>(targetUser.getFcmTokens());
		copy.forEach(token -> send(targetUser, token, title, body));
		saveNotificationLog(targetUser, notification);
	}

	@Async("asyncExecutor")
	@Transactional
	public void sendRejectedAdmissionToUser(String targetUserId, String adminUserId) {
		User targetUser = userRepository.findById(targetUserId).orElse(null);
		if (targetUser == null) {
			return;
		}

		User adminUser = userRepository.findById(adminUserId).orElse(null);
		String title = "서비스 알림";
		String body = "재학정보 인증이 반려되었어요";

		Notification notification = Notification.of(
			adminUser,
			title,
			body,
			NoticeType.ADMISSION,
			null,
			null);
		saveNotification(notification);

		fcmUtils.cleanInvalidFcmTokens(targetUser);
		Set<String> copy = new HashSet<>(targetUser.getFcmTokens());
		copy.forEach(token -> send(targetUser, token, title, body));
		saveNotificationLog(targetUser, notification);
	}
}
