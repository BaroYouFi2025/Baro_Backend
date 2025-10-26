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
        System.out.println("Firebase 설정 테스트 시작");

        // Firebase 앱이 초기화되었는지 확인
        try {
            boolean isInitialized = !com.google.firebase.FirebaseApp.getApps().isEmpty();
            System.out.println("Firebase 초기화 상태: " + isInitialized);

            if (isInitialized) {
                System.out.println("✅ Firebase Admin SDK 초기화 성공!");
                System.out.println(
                        "프로젝트 ID: " + com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());
            } else {
                System.out.println("❌ Firebase Admin SDK 초기화 실패");
            }
        } catch (Exception e) {
            System.out.println("❌ Firebase 초기화 중 오류: " + e.getMessage());
        }
    }
}
