package baro.baro.domain.user.entity;

import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users", schema = "youfi")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "uid", nullable = false, unique = true, length = 50)
    private String uid;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "phone_e164", nullable = false, unique = true, length = 16)
    private String phoneE164;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole role;
    
    @Column(name = "profile_url")
    private String profileUrl;
    
    @Column(name = "profile_background_color")
    private String profileBackgroundColor;

    private Integer card;
    
    private Integer level;

    private Integer exp;

    private String title;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * User 생성자 (도메인 로직)
     */
    @Builder
    private User(String uid, String encodedPassword, String phoneE164, String name, LocalDate birthDate) {
        this.uid = uid;
        this.passwordHash = encodedPassword;
        this.phoneE164 = phoneE164;
        this.name = name;
        this.birthDate = birthDate;
        this.role = UserRole.USER;
        this.profileUrl = "http://example.com/default-profile.png";
        this.profileBackgroundColor = "#FEFFED";
        this.card = 1;
        this.level = 1;
        this.exp = 0;
        this.title = "수색 초보자";
        this.isActive = true;
    }

    /**
     * 새로운 사용자를 생성합니다. (Factory Method)
     * 
     * @param uid 사용자 ID
     * @param rawPassword 원문 비밀번호
     * @param phoneE164 E.164 형식 전화번호
     * @param name 이름
     * @param birthDate 생년월일
     * @param passwordEncoder 비밀번호 인코더
     * @return User 엔티티
     */
    public static User createUser(
            String uid, 
            String rawPassword, 
            String phoneE164,
            String name, 
            LocalDate birthDate,
            PasswordEncoder passwordEncoder) {
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return new User(uid, encodedPassword, phoneE164, name, birthDate);
    }

    /**
     * 비밀번호가 일치하는지 확인합니다.
     * 
     * @param rawPassword 확인할 원문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return 일치하면 true
     */
    public boolean verifyPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.passwordHash);
    }

    /**
     * 프로필을 업데이트합니다.
     * null 값은 무시하고 기존 값을 유지합니다.
     * 
     * @param name 이름
     * @param profileUrl 프로필 URL
     * @param profileBackgroundColor 배경색
     * @param title 칭호
     */
    public void updateProfile(String name, String profileUrl, String profileBackgroundColor, String title) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (profileUrl != null) {
            this.profileUrl = profileUrl;
        }
        if (profileBackgroundColor != null) {
            this.profileBackgroundColor = profileBackgroundColor;
        }
        if (title != null) {
            this.title = title;
        }
    }

    /**
     * 경험치를 추가하고 레벨업을 처리합니다.
     * 
     * @param additionalExp 추가할 경험치
     */
    public void addExperience(int additionalExp) {
        this.exp += additionalExp;
        
        // 레벨업 로직 (100 경험치마다 레벨업)
        while (this.exp >= 100 && this.level < 100) {
            this.exp -= 100;
            this.level++;
        }
    }

    /**
     * 사용자 계정을 비활성화합니다.
     * 
     * @param password 비밀번호 확인
     * @param passwordEncoder 비밀번호 인코더
     * @throws UserException 비밀번호가 일치하지 않거나 이미 비활성화된 경우
     */
    public void deactivate(String password, PasswordEncoder passwordEncoder) {
        if (!verifyPassword(password, passwordEncoder)) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }
        
        if (!this.isActive) {
            throw new UserException(UserErrorCode.USER_ALREADY_INACTIVE);
        }
        
        this.isActive = false;
    }

    /**
     * 사용자가 활성 상태인지 확인합니다.
     * 
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }
}

