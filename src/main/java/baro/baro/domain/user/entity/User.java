package baro.baro.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import baro.baro.domain.common.enums.UserRole;
import baro.baro.domain.common.util.ApplicationContextProvider;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

@Entity
@Table(name = "users", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @Column(name = "profile_url")
    private String profileUrl;
    
    @Column(name = "profile_background_color")
    private String profileBackgroundColor;
    
    private Integer card;
    
    @Builder.Default
    private Integer level = 1;
    
    private Integer exp;
    
    private Integer border;
    
    private String title;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * 사용자의 전화번호를 반환합니다.
     * phoneE164 필드의 별칭 메서드입니다.
     *
     * @return 사용자의 전화번호 (E164 형식)
     */
    public String getPhone() {
        return this.phoneE164;
    }

    /**
     * 현재 로그인된 사용자의 User 엔티티를 반환합니다.
     *
     * @return 현재 로그인된 사용자의 User 엔티티
     * @throws UserException 사용자를 찾을 수 없는 경우
     */
    public static User getCurrentUser() {
        String uid = getCurrentUserUid();
        UserRepository userRepository = ApplicationContextProvider.getBean(UserRepository.class);

        return userRepository.findByUid(uid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * SecurityContext에서 현재 인증된 사용자의 UID를 반환합니다.
     *
     * @return 현재 사용자의 UID (String)
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
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

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     *
     * @return 인증된 경우 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }
}

