package net.causw.app.main.shared.infra.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Value("${fcm.firebase-key-base64}")
	private String firebaseKeyBase64;

	@Bean
	public FirebaseApp firebaseApp() {
		try {
			FirebaseApp firebaseApp = FirebaseApp.getInstance();
			log.info("기본 FirebaseApp이 이미 초기화되어 있습니다.");
			return firebaseApp;
		} catch (IllegalStateException e) {
			return initializeDefaultFirebaseApp();
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}

	private FirebaseApp initializeDefaultFirebaseApp() {
		if (firebaseKeyBase64 == null || firebaseKeyBase64.isBlank()) {
			throw new IllegalStateException("Firebase 초기화 실패: FCM_FIREBASE_KEY_BASE64 환경변수가 없습니다.");
		}

		try {
			byte[] decodedKey = Base64.getMimeDecoder().decode(firebaseKeyBase64.trim());

			try (InputStream credentialStream = new ByteArrayInputStream(decodedKey)) {
				FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(credentialStream))
					.build();

				FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
				log.info("기본 FirebaseApp이 성공적으로 초기화되었습니다.");
				return firebaseApp;
			}
		} catch (IOException | IllegalArgumentException e) {
			log.error("Firebase 초기화 중 에러 발생", e);
			throw new IllegalStateException("Firebase 초기화 실패", e);
		}
	}
}
