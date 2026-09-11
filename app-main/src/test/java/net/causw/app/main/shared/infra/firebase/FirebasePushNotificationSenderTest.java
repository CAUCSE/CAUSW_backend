package net.causw.app.main.shared.infra.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;

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
		FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment, firebaseMessaging);
		PushNotificationData dummyData = new PushNotificationData(null, NoticeType.SYSTEM, null, null);

		// when
		sender.send("token", "title", "body", dummyData);

		// then
		verifyNoInteractions(firebaseMessaging);
	}

	@ParameterizedTest
	@ValueSource(strings = {"dev", "prod"})
	@DisplayName("dev 또는 prod profile에서는 Firebase 전송을 수행한다")
	void givenDevOrProdProfile_whenSend_thenSendFirebaseMessage(String profile) throws Exception {
		// given
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles(profile);
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment, messaging);
		PushNotificationData dummyData = new PushNotificationData(null, NoticeType.SYSTEM, null, null);
		when(messaging.send(any(Message.class))).thenReturn("message-id");

		// when
		sender.send("token", "title", "body", dummyData);

		// then
		verify(messaging).send(any(Message.class));
	}

	@Test
	@DisplayName("noticeType은 항상 포함되고, targetId/targetParentId/notificationLogId는 null이 아닐 때만 data에 포함된다")
	void givenFullData_whenBuildData_thenIncludeAllNonNullFields() {
		// given
		MockEnvironment environment = new MockEnvironment();
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment, messaging);
		PushNotificationData data = new PushNotificationData("logId", NoticeType.COMMUNITY, "targetId",
			"targetParentId");

		// when
		Map<String, String> result = sender.buildData(data);

		// then
		assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
			"noticeType", "COMMUNITY",
			"targetId", "targetId",
			"targetParentId", "targetParentId",
			"notificationLogId", "logId"));
	}

	@Test
	@DisplayName("targetId/targetParentId/notificationLogId가 null이면 data에서 생략된다")
	void givenOptionalFieldsNull_whenBuildData_thenOmitThem() {
		// given
		MockEnvironment environment = new MockEnvironment();
		FirebaseMessaging messaging = mock(FirebaseMessaging.class);
		FirebasePushNotificationSender sender = new FirebasePushNotificationSender(environment, messaging);
		PushNotificationData data = new PushNotificationData(null, NoticeType.SYSTEM, null, null);

		// when
		Map<String, String> result = sender.buildData(data);

		// then
		assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of("noticeType", "SYSTEM"));
	}
}
