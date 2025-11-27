package baro.baro.domain.common.util;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * Security 관련 유틸리티 클래스
 *
 * JwtAuthenticationFilter에서 User 객체를 SecurityContext에 저장하므로
 * DB 재조회 없이 현재 사용자 정보를 가져올 수 있습니다.
 */
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * SecurityContext에서 현재 인증된 사용자를 반환합니다.
     * JwtAuthenticationFilter에서 User 객체를 principal로 저장하므로 DB 조회 없이 반환 가능
     *
     * @return 현재 사용자 엔티티
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }

        Object principal = authentication.getPrincipal();

        if ("anonymousUser".equals(principal)) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new BusinessException(ErrorCode.AUTH_ERROR);
    }

    /**
     * SecurityContext에서 현재 인증된 사용자의 UID를 반환합니다.
     *
     * @return 현재 사용자의 UID (String)
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static String getCurrentUserUid() {
        User user = getCurrentUser();
        return user.getUid();
    }

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     *
     * @return 인증된 경우 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            return false;
        }

        return authentication.isAuthenticated() && principal instanceof User;
    }

    /**
     * SecurityContext에서 현재 인증된 사용자의 deviceId를 반환합니다.
     * JWT 토큰에서 추출된 값입니다.
     *
     * @return 현재 deviceId (없으면 null)
     */
    @SuppressWarnings("unchecked")
    public static Long getCurrentDeviceId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof Map) {
            return (Long) ((Map<String, Object>) details).get("deviceId");
        }

        return null;
    }

    /**
     * SecurityContext에서 현재 인증된 사용자의 role을 반환합니다.
     * JWT 토큰에서 추출된 값입니다.
     *
     * @return 현재 role (없으면 null)
     */
    @SuppressWarnings("unchecked")
    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object details = authentication.getDetails();
        if (details instanceof Map) {
            return (String) ((Map<String, Object>) details).get("role");
        }

        return null;
    }
}
