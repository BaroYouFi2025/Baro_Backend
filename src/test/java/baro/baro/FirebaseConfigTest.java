package baro.baro;

import baro.baro.config.FirebaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Firebase 설정 테스트
 */
@SpringBootTest(classes = { FirebaseConfig.class })
@TestPropertySource(properties = {
        "firebase.project-id=baroyofi",
        "firebase.credentials.path=classpath:firebase-adminsdk.json"
})
public class FirebaseConfigTest {

    @Test
    public void testFirebaseInitialization() {
        // Firebase 설정이 정상적으로 로드되는지 확인
        // Firebase 앱이 초기화되었는지 확인
        boolean isInitialized = !com.google.firebase.FirebaseApp.getApps().isEmpty();
    }
}
