package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.req.UserSearchRequest;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.UserPublicProfileResponse;
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
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.common.monitoring.MetricsService;

import java.time.LocalDate;
import baro.baro.domain.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final GpsTrackRepository gpsTrackRepository;
    private final MetricsService metricsService;

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
        String access = jwtTokenProvider.createAccessToken(
                user.getUid(),
                user.getRole().name(),
                null  // 회원가입 시에는 deviceId 없음
        );
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

        // 메트릭 기록: 회원가입 성공
        metricsService.recordUserRegistration();

        return new AuthTokensResponse(access, refresh, expiresIn);
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

    @Override
    @Transactional(readOnly = true)
    public Slice<UserPublicProfileResponse> searchUsers(UserSearchRequest request) {
        log.debug("사용자 검색 - UID: {}, page: {}, size: {}", 
                  request.getUid(), request.getPage(), request.getSize());
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Slice<User> users;
        
        // UID가 비어있으면 자신의 위치 기준으로 가까운 사용자 조회
        if (request.getUid() == null || request.getUid().trim().isEmpty()) {
            users = searchNearbyUsers(pageable);
        } else {
            // UID로 검색 (부분 일치)
            users = userRepository.findByUidContainingAndIsActiveTrue(request.getUid(), pageable);
        }
        
        log.debug("사용자 검색 결과 - 조회된 사용자 수: {}", users.getNumberOfElements());
        
        return users.map(user -> UserPublicProfileResponse.builder()
                .uid(user.getUid())
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .profileBackgroundColor(user.getProfileBackgroundColor())
                .build());
    }
    
    // 현재 로그인한 사용자의 GPS 위치 기준으로 가까운 사용자를 조회합니다.
    private Slice<User> searchNearbyUsers(Pageable pageable) {
        // 1. 현재 로그인한 사용자의 최근 GPS 위치 조회
        String currentUid = SecurityUtil.getCurrentUserUid();
        User currentUser = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        
        GpsTrack currentLocation = gpsTrackRepository.findLatestByUser(currentUser)
                .orElse(null);
        
        // 2. GPS 위치가 없으면 일반 조회
        if (currentLocation == null || currentLocation.getLocation() == null) {
            log.debug("현재 사용자의 GPS 위치 없음 - 일반 조회로 대체");
            return userRepository.findAllByIsActiveTrue(pageable);
        }
        
        // 3. GPS 위치 기준으로 가까운 사용자 조회
        double latitude = currentLocation.getLocation().getY();
        double longitude = currentLocation.getLocation().getX();
        
        log.debug("현재 위치 기준 사용자 검색 - Lat: {}, Lon: {}", latitude, longitude);
        
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1; // Slice를 위해 +1개 조회
        
        java.util.List<User> userList = userRepository.findNearbyActiveUsers(
                latitude, longitude, limit, offset
        );
        
        // 4. Slice 생성 (다음 페이지 존재 여부 확인)
        boolean hasNext = userList.size() > pageable.getPageSize();
        if (hasNext) {
            userList = userList.subList(0, pageable.getPageSize());
        }
        
        return new org.springframework.data.domain.SliceImpl<>(userList, pageable, hasNext);
    }
}
