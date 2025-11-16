package baro.baro.domain.common.util;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    // SecurityContext에서 현재 인증된 사용자를 반환합니다.
    //
    // @return 현재 사용자 엔티티
    // @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
    // @throws UserException 사용자를 찾을 수 없는 경우
    public static User getCurrentUser() {
        String uid = getCurrentUserUid();
        UserRepository userRepository = ApplicationContextProvider.getBean(UserRepository.class);

        return userRepository.findByUid(uid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
    
    // SecurityContext에서 현재 인증된 사용자의 UID를 반환합니다.
    //
    // @return 현재 사용자의 UID (String)
    // @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
    public static String getCurrentUserUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }

        Object principal = authentication.getPrincipal();

        if ("anonymousUser".equals(principal)) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }

        if (principal instanceof String) {
            return (String) principal;
        }

        throw new BusinessException(ErrorCode.AUTH_ERROR);
    }

    // 현재 사용자가 인증되었는지 확인합니다.
    //
    // @return 인증된 경우 true, 그렇지 않으면 false
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            return false;
        }

        return authentication.isAuthenticated();
    }
}
