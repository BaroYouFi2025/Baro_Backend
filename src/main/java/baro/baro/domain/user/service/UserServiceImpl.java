package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.exception.PhoneVerificationErrorCode;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.auth.service.JwtTokenProvider;
import baro.baro.domain.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

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
        userRepository.findByUid(uid).ifPresent(u -> { throw new IllegalArgumentException("UID already exists"); });
        String digits = phone.replaceAll("[^0-9]", "");
        phoneVerificationRepository.findByPhoneNumber(digits).orElseThrow(() -> new PhoneVerificationException(PhoneVerificationErrorCode.PHONE_NOT_VERIFIED));
        String e164 = digits.startsWith("0") ? "+82" + digits.substring(1) : "+" + digits;

        userRepository.findByPhoneE164(e164).ifPresent(u -> { throw new IllegalArgumentException("Phone already exists"); });

        User user = User.builder()
                .uid(uid)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .phoneE164(e164)
                .name(name)
                .birthDate(LocalDate.parse(birthDateIso))
                .build();
        return userRepository.save(user);
    }

    @Override
    public AuthTokensResponse signup(SignupRequest request, HttpServletResponse response) {
        User user = createUser
                (
                request.getUid(),
                request.getPassword(),
                request.getPhone(),
                request.getUsername(),
                request.getBirthDate()
        );

        String access = jwtTokenProvider.createAccessToken(user.getUid());
        String refresh = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        // Refresh Token을 Cookie에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refresh);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/auth/refresh");
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        log.debug("프로필 조회 성공 - User ID: {}", user.getId());
        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .level(user.getLevel())
                .exp(user.getExp())
                .title(user.getTitle())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        String currentUid = SecurityUtil.getCurrentUserUid();
        User user = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user = User.builder()
                .id(user.getId())
                .uid(user.getUid())
                .passwordHash(user.getPasswordHash())
                .phoneE164(user.getPhone())
                .name(request.getName() != null ? request.getName() : user.getName())
                .birthDate(user.getBirthDate())
                .level(user.getLevel())
                .exp(user.getExp())
                .title(request.getTitle() != null ? request.getTitle() : user.getTitle())
                .isActive(user.getIsActive())
                .build();
        
        userRepository.save(user);
        
        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .level(user.getLevel())
                .exp(user.getExp())
                .title(user.getTitle())
                .build();
    }

    @Override
    @Transactional
    public DeleteUserResponse deleteUser(DeleteUserRequest request) {
        String currentUid = SecurityUtil.getCurrentUserUid();
        User user = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 사용자 비활성화 (실제 삭제 대신)
        User updatedUser = User.builder()
                .id(user.getId())
                .uid(user.getUid())
                .passwordHash(user.getPasswordHash())
                .phoneE164(user.getPhone())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .level(user.getLevel())
                .exp(user.getExp())
                .title(user.getTitle())
                .isActive(false)
                .build();
        
        userRepository.save(updatedUser);
        
        return DeleteUserResponse.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .build();
    }
}
