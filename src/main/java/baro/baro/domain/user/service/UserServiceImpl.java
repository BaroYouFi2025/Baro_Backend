package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.auth.service.JwtTokenProvider;
import baro.baro.domain.auth.exception.PhoneVerificationErrorCode;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import baro.baro.domain.common.util.PhoneNumberUtil;

import java.time.LocalDate;
import baro.baro.domain.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    @Transactional
    public User createUser(String uid, String rawPassword, String phone, String name, String birthDateIso) {
        // 1. UID 중복 검증
        userRepository.findByUid(uid).ifPresent(u -> { 
            throw new UserException(UserErrorCode.USER_ALREADY_EXISTS); 
        });
        
        // 2. 전화번호 인증 확인
        String digits = phone.replaceAll("[^0-9]", "");
        phoneVerificationRepository.findByPhoneNumber(digits)
                .orElseThrow(() -> new PhoneVerificationException(PhoneVerificationErrorCode.PHONE_NOT_VERIFIED));
        
        // 3. 전화번호를 E.164 형식으로 변환
        String e164 = PhoneNumberUtil.toE164Format(digits);
        
        // 4. 전화번호 중복 검증
        userRepository.findByPhoneE164(e164).ifPresent(u -> { 
            throw new UserException(UserErrorCode.PHONE_ALREADY_EXISTS); 
        });

        // 5. 도메인 Factory Method를 통한 User 생성
        User user = User.createUser(
                uid, 
                rawPassword, 
                e164,
                name, 
                LocalDate.parse(birthDateIso),
                passwordEncoder
        );
        
        return userRepository.save(user);
    }

    @Override
    public AuthTokensResponse signup(SignupRequest request, HttpServletResponse response) {
        // User 생성 (도메인 로직 활용)
        User user = createUser(
                request.getUid(),
                request.getPassword(),
                request.getPhone(),
                request.getUsername(),
                request.getBirthDate()
        );

        // JWT 토큰 발급
        String access = jwtTokenProvider.createAccessToken(user.getUid());
        String refresh = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        // Refresh Token을 Cookie에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refresh);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/auth");
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(refreshTokenCookie);

        return new AuthTokensResponse(access, expiresIn);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        String currentUid = SecurityUtil.getCurrentUserUid();
        log.debug("프로필 조회 요청 - UID: {}", currentUid);
        
        User user = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        
        log.debug("프로필 조회 성공 - User ID: {}", user.getId());
        return UserProfileResponse.from(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        // 도메인 메서드를 통한 프로필 업데이트
        user.updateProfile(
                request.getName(), 
                request.getProfileUrl(), 
                request.getProfileBackgroundColor(),
                request.getTitle()
        );

        return UserProfileResponse.from(user);
    }

    @Override
    @Transactional
    public DeleteUserResponse deleteUser(DeleteUserRequest request) {
        String currentUid = SecurityUtil.getCurrentUserUid();
        User user = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        
        // 도메인 메서드를 통한 비활성화 (비밀번호 검증 포함)
        user.deactivate(request.getPassword(), passwordEncoder);
        
        return DeleteUserResponse.create("회원 탈퇴가 완료되었습니다.");
    }
}
