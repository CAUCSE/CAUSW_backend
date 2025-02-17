package net.causw.config.firebase;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // resources 폴더에서 서비스 계정 JSON 파일 로드
                InputStream serviceAccount = new ClassPathResource("dongnae_firebase_key.json").getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase가 성공적으로 초기화되었습니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
}
