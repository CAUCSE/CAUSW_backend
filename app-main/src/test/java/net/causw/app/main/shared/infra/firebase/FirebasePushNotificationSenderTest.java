package net.causw.app.main.shared.infra.firebase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.mock.env.MockEnvironment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

@DisplayName("FirebasePushNotificationSender 단위 테스트")
class FirebasePushNotificationSenderTest {

	@Test
	@DisplayName("local profile에서는 Firebase 전송을 수행하지 않는다")
	void givenLocalProfile_whenSend_thenSkipFirebaseSend() throws Exception {
		// given
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("local");
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment);

		try (MockedStatic<FirebaseMessaging> firebaseMessaging = mockStatic(FirebaseMessaging.class)) {
			// when
			sender.send("token", "title", "body");

			// then
			firebaseMessaging.verifyNoInteractions();
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"dev", "prod"})
	@DisplayName("dev 또는 prod profile에서는 Firebase 전송을 수행한다")
	void givenDevOrProdProfile_whenSend_thenSendFirebaseMessage(String profile) throws Exception {
		// given
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles(profile);
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment);
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);

		try (MockedStatic<FirebaseMessaging> firebaseMessaging = mockStatic(FirebaseMessaging.class)) {
			firebaseMessaging.when(FirebaseMessaging::getInstance).thenReturn(messaging);
			when(messaging.send(any(Message.class))).thenReturn("message-id");

			// when
			sender.send("token", "title", "body");

			// then
			verify(messaging).send(any(Message.class));
		}
	}
}
