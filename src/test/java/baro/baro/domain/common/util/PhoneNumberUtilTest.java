package baro.baro.domain.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberUtilTest {

    @Test
    void toE164Format_whenNumberAlreadyContainsCountryCode_returnsNormalizedNumber() {
        String result = PhoneNumberUtil.toE164Format("+82-10-1234-5678");

        assertThat(result).isEqualTo("+821012345678");
    }

    @Test
    void toE164Format_whenLocalNumberStartsWithZero_replacesPrefixWithCountryCode() {
        String result = PhoneNumberUtil.toE164Format("010-1234-5678");

        assertThat(result).isEqualTo("+821012345678");
    }

    @Test
    void toE164Format_whenNumberWithoutPrefix_addsKoreanCountryCode() {
        String result = PhoneNumberUtil.toE164Format("1012345678");

        assertThat(result).isEqualTo("+821012345678");
    }

    @Test
    void toE164Format_whenEmpty_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PhoneNumberUtil.toE164Format(""));

        assertThat(exception).hasMessage("전화번호는 필수 입력값입니다.");
    }

    @Test
    void toLocalFormat_whenStartsWithPlus82_returnsLocalNumber() {
        String result = PhoneNumberUtil.toLocalFormat("+821012345678");

        assertThat(result).isEqualTo("01012345678");
    }

    @Test
    void toLocalFormat_whenStartsWith82WithoutPlus_returnsLocalNumber() {
        String result = PhoneNumberUtil.toLocalFormat("821012345678");

        assertThat(result).isEqualTo("01012345678");
    }

    @Test
    void toLocalFormat_whenAlreadyLocal_returnsInput() {
        String result = PhoneNumberUtil.toLocalFormat("01012345678");

        assertThat(result).isEqualTo("01012345678");
    }

    @Test
    void toLocalFormat_whenNull_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PhoneNumberUtil.toLocalFormat(null));

        assertThat(exception).hasMessage("전화번호는 필수 입력값입니다.");
    }

    @Test
    void isValidKoreanPhoneNumber_returnsTrueFor10Or11Digits() {
        assertThat(PhoneNumberUtil.isValidKoreanPhoneNumber("010-123-4567")).isTrue();
        assertThat(PhoneNumberUtil.isValidKoreanPhoneNumber("01012345678")).isTrue();
    }

    @Test
    void isValidKoreanPhoneNumber_returnsFalseForInvalidInputs() {
        assertThat(PhoneNumberUtil.isValidKoreanPhoneNumber(null)).isFalse();
        assertThat(PhoneNumberUtil.isValidKoreanPhoneNumber("012345678")).isFalse();
        assertThat(PhoneNumberUtil.isValidKoreanPhoneNumber("010123456789")).isFalse();
    }
}
