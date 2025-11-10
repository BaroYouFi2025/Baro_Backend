package baro.baro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Firebase 설정 클래스
 * 
 * Firebase Admin SDK를 초기화하고 FCM 서비스를 사용할 수 있도록 설정합니다.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        try {
            // Firebase가 이미 초기화되었는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                // 서비스 계정 키 파일 로드
                ClassPathResource resource = new ClassPathResource(credentialsPath);
                GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

                // Firebase 옵션 설정
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();

                // Firebase 앱 초기화
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료 - 프로젝트 ID: {}", projectId);
            } else {
                log.info("Firebase Admin SDK가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase 초기화 실패", e);
        }
    }
}
