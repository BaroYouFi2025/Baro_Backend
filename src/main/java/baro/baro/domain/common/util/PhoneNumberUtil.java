package baro.baro.domain.common.util;

// 전화번호 관련 유틸리티 클래스
public class PhoneNumberUtil {

    // 한국 전화번호를 E.164 형식으로 변환합니다.
    //
    // @param phone 전화번호 (숫자만 포함하거나 하이픈 포함 가능)
    // @return E.164 형식 전화번호 (예: +821012345678)
    public static String toE164Format(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수 입력값입니다.");
        }

        // 숫자만 추출
        String digits = phone.replaceAll("[^0-9]", "");

        // 이미 국가코드가 포함된 경우 (82로 시작)
        if (digits.startsWith("82")) {
            return "+" + digits;
        }

        // 한국 지역번호로 시작하는 경우 (0으로 시작)
        if (digits.startsWith("0")) {
            return "+82" + digits.substring(1);
        }

        // 국가코드 없이 지역번호도 없는 경우 (바로 번호만)
        // 예: 1012345678 -> +821012345678
        return "+82" + digits;
    }

    // E.164 형식의 전화번호를 로컬 형식으로 변환합니다.
    //
    // @param e164Phone E.164 형식 전화번호 (예: +821012345678)
    // @return 로컬 형식 전화번호 (예: 01012345678)
    public static String toLocalFormat(String e164Phone) {
        if (e164Phone == null || e164Phone.isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수 입력값입니다.");
        }

        // +82로 시작하는 경우
        if (e164Phone.startsWith("+82")) {
            return "0" + e164Phone.substring(3);
        }

        // 82로 시작하는 경우
        if (e164Phone.startsWith("82")) {
            return "0" + e164Phone.substring(2);
        }

        // 이미 로컬 형식인 경우
        return e164Phone;
    }

    // 전화번호 유효성을 검증합니다.
    //
    // @param phone 전화번호
    // @return 유효한 전화번호인 경우 true
    public static boolean isValidKoreanPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        String digits = phone.replaceAll("[^0-9]", "");

        // 한국 휴대폰 번호는 10자리 또는 11자리
        return digits.length() >= 10 && digits.length() <= 11;
    }
}
