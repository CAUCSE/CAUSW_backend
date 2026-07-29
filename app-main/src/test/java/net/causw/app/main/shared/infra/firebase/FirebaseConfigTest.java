package net.causw.app.main.shared.infra.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@DisplayName("FirebaseConfig 단위 테스트")
class FirebaseConfigTest {

	@Test
	@DisplayName("기본 FirebaseApp이 존재하면 기존 앱을 반환한다")
	void givenDefaultFirebaseApp_whenCreateBean_thenReturnExistingApp() {
		// given
		FirebaseConfig firebaseConfig = new FirebaseConfig();
		FirebaseApp existingApp = mock(FirebaseApp.class);

		try (MockedStatic<FirebaseApp> firebaseApp = mockStatic(FirebaseApp.class)) {
			firebaseApp.when(FirebaseApp::getInstance).thenReturn(existingApp);

			// when
			FirebaseApp result = firebaseConfig.firebaseApp();

			// then
			assertThat(result).isSameAs(existingApp);
			firebaseApp.verify(FirebaseApp::getInstance);
			firebaseApp.verifyNoMoreInteractions();
		}
	}

	@Test
	@DisplayName("이름이 지정된 앱만 있고 기본 FirebaseApp이 없으면 기본 앱을 초기화한다")
	void givenNoDefaultFirebaseApp_whenCreateBean_thenInitializeDefaultApp() {
		// given
		FirebaseConfig firebaseConfig = new FirebaseConfig();
		String credentialJson = "{\"type\":\"service_account\"}";
		String encodedCredential = Base64.getEncoder()
			.encodeToString(credentialJson.getBytes(StandardCharsets.UTF_8));
		ReflectionTestUtils.setField(firebaseConfig, "firebaseKeyBase64", encodedCredential);

		FirebaseApp initializedApp = mock(FirebaseApp.class);
		GoogleCredentials googleCredentials = mock(GoogleCredentials.class);

		try (
			MockedStatic<FirebaseApp> firebaseApp = mockStatic(FirebaseApp.class);
			MockedStatic<GoogleCredentials> credentials = mockStatic(GoogleCredentials.class)) {
			firebaseApp.when(FirebaseApp::getInstance)
				.thenThrow(new IllegalStateException("FirebaseApp with name [DEFAULT] doesn't exist."));
			firebaseApp.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class))).thenReturn(initializedApp);
			credentials.when(() -> GoogleCredentials.fromStream(any())).thenReturn(googleCredentials);

			// when
			FirebaseApp result = firebaseConfig.firebaseApp();

			// then
			assertThat(result).isSameAs(initializedApp);
			firebaseApp.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)));
		}
	}
}
