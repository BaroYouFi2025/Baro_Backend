package baro.baro;

import baro.baro.domain.notification.service.PushNotificationService;
import baro.baro.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 푸시 알림 서비스 테스트
 */
@SpringBootTest
@TestPropertySource(properties = {
        "firebase.project-id=baroyofi",
        "firebase.credentials.path=classpath:firebase-adminsdk.json"
})
public class PushNotificationTest {

    @Test
    public void testPushNotificationService() {
        // 테스트용 사용자 생성
        User inviter = User.builder()
                .uid("test-inviter")
                .name("초대자")
                .phoneE164("+821012345678")
                .birthDate(java.time.LocalDate.of(1990, 1, 1))
                .encodedPassword("password123")
                .build();

        User invitee = User.builder()
                .uid("test-invitee")
                .name("초대받은사람")
                .phoneE164("+821087654321")
                .birthDate(java.time.LocalDate.of(1995, 5, 5))
                .encodedPassword("password456")
                .build();

        // 푸시 알림 서비스 테스트 (실제 발송은 하지 않음)
    }
}
