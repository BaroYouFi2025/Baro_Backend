package baro.baro.domain.member.controller;

import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.member.dto.res.MemberLocationResponse;
import baro.baro.domain.member.dto.res.MemberLocationResponse.LocationInfo;
import baro.baro.domain.member.service.MemberLocationEmitterRegistry;
import baro.baro.domain.member.service.MemberService;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberControllerSseTest {

    private static final long EXPECTED_SSE_TIMEOUT = 30 * 60 * 1000L;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberLocationEmitterRegistry emitterRegistry;

    private MemberController memberController;

    @BeforeEach
    void setUp() {
        memberController = new MemberController(memberService, emitterRegistry, new ObjectMapper());
    }

    @Test
    @DisplayName("streamMemberLocations 성공 시 emitter를 등록하고 초기 데이터를 전송한다")
    void streamMemberLocations_success_registersEmitterAndSendsInitialData() throws Exception {
        List<MemberLocationResponse> payload = List.of(
                MemberLocationResponse.create(
                        1L,
                        "Member One",
                        "가족",
                        80,
                        1.5,
                        LocationInfo.create(37.5665, 126.9780)
                )
        );
        when(memberService.getMemberLocations()).thenReturn(payload);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            User currentUser = createUser(42L);
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

            SseEmitter emitter = memberController.streamMemberLocations();

            assertThat(emitter).isNotNull();
            assertThat(emitter.getTimeout()).isEqualTo(EXPECTED_SSE_TIMEOUT);

            ArgumentCaptor<SseEmitter> emitterCaptor = ArgumentCaptor.forClass(SseEmitter.class);
            verify(emitterRegistry).addEmitter(eq(currentUser.getId()), emitterCaptor.capture());
            assertThat(emitterCaptor.getValue()).isSameAs(emitter);
            verify(memberService).getMemberLocations();

            emitter.complete();
        }
    }

    @Test
    @DisplayName("초기 이벤트 직렬화 실패 시 emitter를 오류로 종료한다")
    void streamMemberLocations_initialSerializationFailure_completesWithError() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        memberController = new MemberController(memberService, emitterRegistry, failingMapper);

        List<MemberLocationResponse> payload = List.of(
                MemberLocationResponse.create(
                        1L,
                        "Member One",
                        "가족",
                        80,
                        1.5,
                        LocationInfo.create(37.5665, 126.9780)
                )
        );
        when(memberService.getMemberLocations()).thenReturn(payload);
        when(failingMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            User currentUser = createUser(108L);
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

            SseEmitter emitter = memberController.streamMemberLocations();

            assertThat(emitter).isNotNull();
            verify(emitterRegistry).addEmitter(eq(currentUser.getId()), any(SseEmitter.class));
            verify(memberService).getMemberLocations();
            verify(failingMapper).writeValueAsString(any());
        }
    }

    private User createUser(Long id) {
        User user = User.builder()
                .uid("user-" + id)
                .encodedPassword("encoded-password")
                .phoneE164("+82101234567")
                .name("tester")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", UserRole.USER);
        return user;
    }
}
