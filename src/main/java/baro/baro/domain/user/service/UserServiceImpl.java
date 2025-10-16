package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.exception.PhoneVerificationErrorCode;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.auth.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtTokenProvider jwtTokenProvider;

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
    public AuthTokensResponse signup(SignupRequest request) {
        User user = createUser
                (
                request.getUid(),
                request.getPassword(),
                request.getPhone(),
                request.getUsername(),
                request.getBirthDate()
        );

        String access = jwtTokenProvider.createAccessToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        return new AuthTokensResponse(access, expiresIn);
    }
}
