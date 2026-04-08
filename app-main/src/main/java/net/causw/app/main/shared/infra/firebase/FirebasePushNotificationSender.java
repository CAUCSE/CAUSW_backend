package net.causw.app.main.shared.infra.firebase;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.infra.push.PushNotificationSender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FirebasePushNotificationSender implements PushNotificationSender {

	@Override
	public void send(String token, String title, String body) throws Exception {
		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();

		Message message = Message.builder()
			.setToken(token)
			.setNotification(notification)
			.build();

		String response = FirebaseMessaging.getInstance().send(message);
		log.info("Successfully sent message: {}", response);
	}
}
