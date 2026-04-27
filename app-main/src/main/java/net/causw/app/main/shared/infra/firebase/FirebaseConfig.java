package net.causw.app.main.shared.infra.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Value("${fcm.firebase-key-base64}")
	private String firebaseKeyBase64;

	@PostConstruct
	public void initialize() {
		try {
			if (FirebaseApp.getApps().isEmpty()) {
				if (firebaseKeyBase64 == null || firebaseKeyBase64.isBlank()) {
					throw new RuntimeException("Firebase 초기화 실패: FCM_FIREBASE_KEY_BASE64 환경변수가 없습니다.");
				}
				byte[] decodedKey = Base64.getDecoder().decode(firebaseKeyBase64);

				// 디코딩된 키를 InputStream으로 변환하여 Firebase에 주입
				try (InputStream credentialStream = new ByteArrayInputStream(decodedKey)) {
					FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(credentialStream))
						.build();

					FirebaseApp.initializeApp(options);
					log.info("Firebase가 성공적으로 초기화되었습니다.");
				}
			}
		} catch (IOException e) {
			log.error("Firebase 초기화 중 I/O 에러 발생", e);
			throw new RuntimeException("Firebase 초기화 실패", e);
		}
	}
}
