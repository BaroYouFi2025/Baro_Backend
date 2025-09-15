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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import baro.baro.domain.common.enums.UserRole;

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
    private String nickname;
    
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", columnDefinition = "user_role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @Column(name = "profile_url")
    private String profileUrl;
    
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
}

