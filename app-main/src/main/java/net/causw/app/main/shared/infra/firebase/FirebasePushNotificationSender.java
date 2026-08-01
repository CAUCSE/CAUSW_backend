package net.causw.app.main.shared.infra.firebase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.shared.infra.push.PushNotificationSender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FirebasePushNotificationSender implements PushNotificationSender {

	private static final String PROD_PROFILE = "prod";
	private static final String DEV_PROFILE = "dev";

	private final Environment environment;
	private final FirebaseMessaging firebaseMessaging;

	public FirebasePushNotificationSender(Environment environment, FirebaseMessaging firebaseMessaging) {
		this.environment = environment;
		this.firebaseMessaging = firebaseMessaging;
	}

	@Override
	public void send(String token, String title, String body, PushNotificationData pushNotificationData)
		throws Exception {
		if (!isPushEnabledProfile()) {
			log.debug("FCM 메시지 발송 생략: activeProfiles={}", Arrays.toString(environment.getActiveProfiles()));
			return;
		}

		Map<String, String> data = new HashMap<>();
		data.put("noticeType", pushNotificationData.noticeType().name());

		if (pushNotificationData.targetId() != null) {
			data.put("targetId", pushNotificationData.targetId());
		}

		if (pushNotificationData.targetParentId() != null) {
			data.put("targetParentId", pushNotificationData.targetParentId());
		}

		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();

		Message message = Message.builder()
			.putAllData(data)
			.setToken(token)
			.setNotification(notification)
			.build();

		String response = firebaseMessaging.send(message);
		log.info("FCM 메시지 발송 성공: response={}", response);
	}

	private boolean isPushEnabledProfile() {
		return Arrays.stream(environment.getActiveProfiles())
			.anyMatch(profile -> PROD_PROFILE.equals(profile) || DEV_PROFILE.equals(profile));
	}
}
