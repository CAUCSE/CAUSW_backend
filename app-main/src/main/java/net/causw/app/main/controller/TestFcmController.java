package net.causw.app.main.controller;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import net.causw.app.main.domain.moving.dto.user.UserFcmCreateRequestDto;
import net.causw.app.main.domain.moving.dto.user.UserFcmTokenResponseDto;
import net.causw.app.main.domain.moving.service.notification.FirebasePushNotificationService;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/test-fcm")
@RequiredArgsConstructor
@Slf4j
public class TestFcmController {
	//테스트 용으로 메모리에 저장
	private final Map<String, String> tokenStorage = new ConcurrentHashMap<>();
	private final FirebasePushNotificationService firebasePushNotificationService;

	public void sendNotification(String targetToken) throws Exception {
		Notification notification = Notification.builder().setTitle("타이틀").setBody("바디").build();

		Map<String, String> data = new HashMap<>();

		data.put("targetId", "6c014e64-5481-4fca-a0f3-e206c31ff58c");
		data.put("targetParentId", "0151b506-813f-4c31-86d6-303f873ecc07");

		Message message = Message.builder().setToken(targetToken).setNotification(notification).putAllData(data).build();

		String response = FirebaseMessaging.getInstance().send(message);
		log.info("Successfully sent message: " + response);
	}

	public void send(String targetToken) {
		try {
			sendNotification(targetToken);
		} catch (FirebaseMessagingException e) {
			log.warn("FCM 전송 실패: {}, 이유: {}", targetToken, e.getMessage());
			log.info("오류 발생으로 FCM 토큰 제거됨: {}", targetToken);
		} catch (Exception e) {
			log.error("FCM 전송 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
		}
	}

	@PostMapping(value = "/register")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "사용자 fcmToken 등록")
	public UserFcmTokenResponseDto registerFcmToken(@RequestBody UserFcmCreateRequestDto userFcmCreateRequestDto) {
		tokenStorage.put(userFcmCreateRequestDto.getRefreshToken(), userFcmCreateRequestDto.getFcmToken());
		return UserFcmTokenResponseDto.builder().fcmToken(List.of(userFcmCreateRequestDto.getFcmToken())).build();
	}

	@PostMapping("/send")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "테스트 알림 발송")
	public String sendNotification(@RequestBody Map<String, String> request) {
		String token = request.get("fcmToken");
		String userId = request.get("refreshToken");

		send(token);

		return userId + "에게 푸시 발송 시도함 (Token: " + token + ")";
	}
}
