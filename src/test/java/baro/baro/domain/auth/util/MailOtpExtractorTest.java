package baro.baro.domain.auth.util;

import baro.baro.domain.auth.exception.EmailException;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailOtpExtractorTest {

    private Session session = Session.getDefaultInstance(new Properties());

    @Test
    @DisplayName("제목과 본문에서 6자리 OTP를 정확히 추출한다")
    void extract_withOtpInSubject_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01012345678@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호: 123456");
        message.setText("귀하의 인증번호는 123456입니다.");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.code()).isEqualTo("123456");
    }

    @Test
    @DisplayName("공백이 섞인 OTP(2 7 1 6 2 9 형태)를 정확히 추출한다")
    void extract_withSpacedOtp_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01087654321@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호");
        message.setText("인증번호: 2 7 1 6 2 9");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01087654321");
        assertThat(result.code()).isEqualTo("271629");
    }

    @Test
    @DisplayName("010으로 시작하는 11자리 전화번호를 추출한다")
    void extract_with010PhoneNumber_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01099998888@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호");
        message.setText("인증코드: 987654");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01099998888");
        assertThat(result.code()).isEqualTo("987654");
    }

    @Test
    @DisplayName("011, 016, 017, 018, 019로 시작하는 전화번호도 추출한다")
    void extract_withVariousPhonePrefixes_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01612345678@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호");
        message.setText("코드: 555666");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01612345678");
        assertThat(result.code()).isEqualTo("555666");
    }

    @Test
    @DisplayName("HTML 이메일에서도 OTP를 추출한다")
    void extract_fromHtmlEmail_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01011112222@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호");
        message.setContent(
                "<html><body><p>인증번호는 <strong>654321</strong>입니다.</p></body></html>",
                "text/html; charset=UTF-8"
        );

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01011112222");
        assertThat(result.code()).isEqualTo("654321");
    }

    @Test
    @DisplayName("전화번호가 로컬파트에 없으면 예외를 던진다")
    void extract_withoutPhoneNumber_throwsException() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("noreply@example.com"));
        message.setSubject("인증번호");
        message.setText("인증코드: 123456");

        // When & Then
        assertThatThrownBy(() -> MailOtpExtractor.extract(message))
                .isInstanceOf(EmailException.class);
    }

    @Test
    @DisplayName("OTP 코드가 없으면 예외를 던진다")
    void extract_withoutOtpCode_throwsException() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01012345678@mmsmail.uplus.co.kr"));
        message.setSubject("알림");
        message.setText("인증번호가 없는 메시지입니다.");

        // When & Then
        assertThatThrownBy(() -> MailOtpExtractor.extract(message))
                .isInstanceOf(EmailException.class);
    }

    @Test
    @DisplayName("From 주소가 없으면 예외를 던진다")
    void extract_withoutFromAddress_throwsException() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setSubject("인증번호");
        message.setText("인증코드: 123456");

        // When & Then
        assertThatThrownBy(() -> MailOtpExtractor.extract(message))
                .isInstanceOf(EmailException.class);
    }

    @Test
    @DisplayName("본문에 여러 숫자가 있을 때 첫 6자리 OTP를 추출한다")
    void extract_withMultipleNumbers_extractsFirst6Digits() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01012345678@mmsmail.uplus.co.kr"));
        message.setSubject("인증번호");
        message.setText("주문번호: 202512345, 인증코드: 999888, 금액: 50000원");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.code()).isEqualTo("999888");
    }

    @Test
    @DisplayName("제목에만 OTP가 있어도 추출한다")
    void extract_withOtpOnlyInSubject_extractsCorrectly() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01012345678@mmsmail.uplus.co.kr"));
        message.setSubject("123456");
        message.setText("인증번호를 확인하세요.");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.code()).isEqualTo("123456");
    }

    @Test
    @DisplayName("OTP가 5자리 이하면 최후 보정으로 숫자 앞 6자리를 추출한다")
    void extract_withLessThan6Digits_usesFirstSixDigitsAsLastResort() throws Exception {
        // Given
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("01012345678@mmsmail.uplus.co.kr"));
        message.setSubject("인증");
        message.setText("코드는 1-2-3-4-5-6입니다. 시간: 14:30");

        // When
        MailOtpExtractor.Result result = MailOtpExtractor.extract(message);

        // Then
        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.code()).isEqualTo("123456");
    }
}
