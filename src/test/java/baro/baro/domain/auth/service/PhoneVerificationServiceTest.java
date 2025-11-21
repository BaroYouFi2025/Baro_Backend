package baro.baro.domain.auth.service;

import baro.baro.domain.auth.entity.PhoneVerification;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationServiceTest {

    @Mock
    private PhoneVerificationRepository repo;

    @InjectMocks
    private PhoneVerificationService service;

    @Test
    @DisplayName("6자리 인증 토큰을 성공적으로 생성한다")
    void createVerificationToken_success() {
        // Given
        when(repo.findByTokenAndVerifiedFalse(anyString())).thenReturn(Optional.empty());
        when(repo.save(any(PhoneVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String token = service.createVerificationToken();

        // Then
        assertThat(token).isNotNull();
        assertThat(token).hasSize(6);
        assertThat(token).matches("\\d{6}");

        ArgumentCaptor<PhoneVerification> captor = ArgumentCaptor.forClass(PhoneVerification.class);
        verify(repo).save(captor.capture());

        PhoneVerification saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo(token);
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("중복되지 않는 토큰을 생성한다")
    void createVerificationToken_avoidsDuplicates() {
        // Given
        when(repo.findByTokenAndVerifiedFalse(anyString()))
                .thenReturn(Optional.of(PhoneVerification.builder().build())) // 첫 시도: 중복
                .thenReturn(Optional.empty()); // 두 번째 시도: 성공
        when(repo.save(any(PhoneVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String token = service.createVerificationToken();

        // Then
        assertThat(token).isNotNull();
        verify(repo, atLeast(2)).findByTokenAndVerifiedFalse(anyString());
    }

    @Test
    @DisplayName("50번 재시도 후에도 중복이면 예외를 던진다")
    void createVerificationToken_failsAfter50Retries() {
        // Given
        when(repo.findByTokenAndVerifiedFalse(anyString()))
                .thenReturn(Optional.of(PhoneVerification.builder().build()));

        // When & Then
        assertThatThrownBy(() -> service.createVerificationToken())
                .isInstanceOf(PhoneVerificationException.class);

        // retryCount > 50일 때 예외를 던지므로, 51번째 시도에서 예외 발생
        // 하지만 while 조건 체크는 50번만 실행됨 (51번째는 체크 전에 예외 발생)
        verify(repo, times(50)).findByTokenAndVerifiedFalse(anyString());
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("유효한 토큰으로 전화번호 인증에 성공한다")
    void authenticateWithToken_success() {
        // Given
        String token = "123456";
        String phoneNumber = "01012345678";
        PhoneVerification pv = PhoneVerification.builder()
                .token(token)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(repo.findByTokenAndVerifiedFalse(token)).thenReturn(Optional.of(pv));

        // When
        service.authenticateWithToken(token, phoneNumber);

        // Then
        assertThat(pv.isVerified()).isTrue();
        assertThat(pv.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("만료된 토큰으로 인증 시 예외를 던진다")
    void authenticateWithToken_withExpiredToken_throwsException() {
        // Given
        String token = "123456";
        String phoneNumber = "01012345678";
        PhoneVerification pv = PhoneVerification.builder()
                .token(token)
                .verified(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // 1분 전 만료
                .build();

        when(repo.findByTokenAndVerifiedFalse(token)).thenReturn(Optional.of(pv));

        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken(token, phoneNumber))
                .isInstanceOf(PhoneVerificationException.class);
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 인증 시 예외를 던진다")
    void authenticateWithToken_withInvalidToken_throwsException() {
        // Given
        String token = "999999";
        String phoneNumber = "01012345678";
        when(repo.findByTokenAndVerifiedFalse(token)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken(token, phoneNumber))
                .isInstanceOf(PhoneVerificationException.class);
    }

    @Test
    @DisplayName("null 토큰으로 인증 시 예외를 던진다")
    void authenticateWithToken_withNullToken_throwsException() {
        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken(null, "01012345678"))
                .isInstanceOf(PhoneVerificationException.class);

        verify(repo, never()).findByTokenAndVerifiedFalse(anyString());
    }

    @Test
    @DisplayName("빈 문자열 토큰으로 인증 시 예외를 던진다")
    void authenticateWithToken_withEmptyToken_throwsException() {
        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken("  ", "01012345678"))
                .isInstanceOf(PhoneVerificationException.class);

        verify(repo, never()).findByTokenAndVerifiedFalse(anyString());
    }

    @Test
    @DisplayName("null 전화번호로 인증 시 예외를 던진다")
    void authenticateWithToken_withNullPhoneNumber_throwsException() {
        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken("123456", null))
                .isInstanceOf(PhoneVerificationException.class);

        verify(repo, never()).findByTokenAndVerifiedFalse(anyString());
    }

    @Test
    @DisplayName("빈 문자열 전화번호로 인증 시 예외를 던진다")
    void authenticateWithToken_withEmptyPhoneNumber_throwsException() {
        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken("123456", "  "))
                .isInstanceOf(PhoneVerificationException.class);

        verify(repo, never()).findByTokenAndVerifiedFalse(anyString());
    }

    @Test
    @DisplayName("인증된 전화번호는 true를 반환한다")
    void isPhoneNumberVerified_withVerifiedNumber_returnsTrue() {
        // Given
        String phoneNumber = "01012345678";
        PhoneVerification pv = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .verified(true)
                .build();

        when(repo.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(pv));

        // When
        boolean isVerified = service.isPhoneNumberVerified(phoneNumber);

        // Then
        assertThat(isVerified).isTrue();
    }

    @Test
    @DisplayName("인증되지 않은 전화번호는 false를 반환한다")
    void isPhoneNumberVerified_withUnverifiedNumber_returnsFalse() {
        // Given
        String phoneNumber = "01012345678";
        PhoneVerification pv = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .verified(false)
                .build();

        when(repo.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(pv));

        // When
        boolean isVerified = service.isPhoneNumberVerified(phoneNumber);

        // Then
        assertThat(isVerified).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 전화번호는 false를 반환한다")
    void isPhoneNumberVerified_withNonExistentNumber_returnsFalse() {
        // Given
        String phoneNumber = "01099999999";
        when(repo.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When
        boolean isVerified = service.isPhoneNumberVerified(phoneNumber);

        // Then
        assertThat(isVerified).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰들을 정리한다")
    void cleanupExpiredTokens_deletesExpiredTokens() {
        // Given
        when(repo.deleteExpiredTokens(any(LocalDateTime.class))).thenReturn(5);

        // When
        service.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repo).deleteExpiredTokens(captor.capture());

        LocalDateTime now = captor.getValue();
        assertThat(now).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(now).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    @DisplayName("만료되지 않은 토큰으로 정상 인증한다")
    void authenticateWithToken_withValidToken_success() {
        // Given
        String token = "654321";
        String phoneNumber = "01087654321";
        PhoneVerification pv = PhoneVerification.builder()
                .token(token)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusSeconds(300)) // 5분 남음
                .build();

        when(repo.findByTokenAndVerifiedFalse(token)).thenReturn(Optional.of(pv));

        // When
        service.authenticateWithToken(token, phoneNumber);

        // Then
        assertThat(pv.isVerified()).isTrue();
        assertThat(pv.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("정확히 만료 시간에 도달한 토큰은 인증에 실패한다")
    void authenticateWithToken_atExactExpiryTime_throwsException() {
        // Given
        String token = "123456";
        String phoneNumber = "01012345678";
        LocalDateTime now = LocalDateTime.now();
        PhoneVerification pv = PhoneVerification.builder()
                .token(token)
                .verified(false)
                .expiresAt(now.minusNanos(1000)) // 아주 조금 지남
                .build();

        when(repo.findByTokenAndVerifiedFalse(token)).thenReturn(Optional.of(pv));

        // When & Then
        assertThatThrownBy(() -> service.authenticateWithToken(token, phoneNumber))
                .isInstanceOf(PhoneVerificationException.class);
    }
}
