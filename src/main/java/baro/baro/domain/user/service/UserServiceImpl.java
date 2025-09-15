package baro.baro.domain.user.service;

import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String uid, String rawPassword, String phone, String nickname, String birthDateIso) {
        userRepository.findByUid(uid).ifPresent(u -> { throw new IllegalArgumentException("UID already exists"); });
        String digits = phone.replaceAll("[^0-9]", "");
        String e164 = digits.startsWith("0") ? "+82" + digits.substring(1) : "+" + digits;

        userRepository.findByPhoneE164(e164).ifPresent(u -> { throw new IllegalArgumentException("Phone already exists"); });

        User user = User.builder()
                .uid(uid)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .phoneE164(e164)
                .nickname(nickname)
                .birthDate(LocalDate.parse(birthDateIso))
                .build();
        return userRepository.save(user);
    }
}
