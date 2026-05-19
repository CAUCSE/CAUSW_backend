package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.infra.push.PushNotificationSender;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushSender {

	private final PushNotificationSender pushNotificationSender;
	private final UserPushTokenWriter userPushTokenWriter;

	/**
	 * 유저의 모든 FCM 토큰으로 푸시 알림을 전송합니다.
	 * 유효하지 않은 토큰은 사전에 정리하고, 전송 실패 토큰은 자동으로 제거합니다.
	 *
	 * @param user  알림을 받을 유저
	 * @param title 알림 제목
	 * @param body  알림 내용
	 */
	public void sendToUser(User user, String title, String body) {
		send(user, title, body);
	}

	/**
	 * 여러 유저에게 일괄로 푸시 알림을 전송합니다.
	 *
	 * @param users 알림을 받을 유저 목록
	 * @param title 알림 제목
	 * @param body  알림 내용
	 */
	public void sendToUsers(List<User> users, String title, String body) {
		users.forEach(user -> send(user, title, body));
	}

	/**
	 * 푸시알림 전송
	 * @param user 유저
	 * @param title 제목
	 * @param body body값
	 */
	private void send(User user, String title, String body) {
		if (user.getFcmTokens() == null) {
			return;
		}

		Set<String> tokens = new HashSet<>(user.getFcmTokens());
		tokens.forEach(token -> trySend(user, token, title, body));
	}

	/**
	 * 단일 FCM 토큰으로 푸시 알림을 전송하는 시도 메서드입니다.
	 *
	 * @param user  타겟 유저
	 * @param token fcm 토큰
	 * @param title 제목
	 * @param body  내용
	 */
	private void trySend(User user, String token, String title, String body) {
		try {
			pushNotificationSender.send(token, title, body);
		} catch (FirebaseMessagingException e) {
			log.error("FCM 전송 실패: {}, 이유: {}", token, e.getMessage());
			userPushTokenWriter.removeFcmToken(user, token);
			log.info("오류 발생으로 FCM 토큰 제거됨: {}", token);
		} catch (Exception e) {
			log.error("FCM 전송 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
		}
	}
}
