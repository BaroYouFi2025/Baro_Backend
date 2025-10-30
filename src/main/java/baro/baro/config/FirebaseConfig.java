package baro.baro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                Resource resource = resourceLoader.getResource(credentialsPath);
                if (!resource.exists()) {
                    log.error("Firebase credentials file not found at: {}", credentialsPath);
                    throw new IOException("Firebase credentials file not found.");
                }

                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료 - 프로젝트 ID: {}", projectId);
            } else {
                log.info("Firebase Admin SDK가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}