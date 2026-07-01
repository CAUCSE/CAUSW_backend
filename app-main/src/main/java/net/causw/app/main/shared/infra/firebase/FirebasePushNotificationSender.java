package net.causw.app.main.shared.infra.firebase;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import net.causw.app.main.shared.infra.push.PushNotificationSender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebasePushNotificationSender implements PushNotificationSender {

	private static final String PROD_PROFILE = "prod";
	private static final String DEV_PROFILE = "dev";

	private final Environment environment;

	@Override
	public void send(String token, String title, String body) throws Exception {
		if (!isPushEnabledProfile()) {
			log.debug("FCM 메시지 발송 생략: activeProfiles={}", Arrays.toString(environment.getActiveProfiles()));
			return;
		}

		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();

		Message message = Message.builder()
			.setToken(token)
			.setNotification(notification)
			.build();

		String response = FirebaseMessaging.getInstance().send(message);
		log.info("FCM 메시지 발송 성공: response={}", response);
	}

	private boolean isPushEnabledProfile() {
		return Arrays.stream(environment.getActiveProfiles())
			.anyMatch(profile -> PROD_PROFILE.equals(profile) || DEV_PROFILE.equals(profile));
	}
}
