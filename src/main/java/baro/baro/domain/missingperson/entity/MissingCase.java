package baro.baro.domain.missingperson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.user.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;

@Entity
@Table(name = "missing_cases", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private CaseStatusType caseStatus = CaseStatusType.OPEN;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;
    
    @CreationTimestamp
    @Column(name = "reported_at", nullable = false)
    private ZonedDateTime reportedAt;
    
    // 실종 케이스 생성 (Factory Method)
    // 빌더 패턴으로 불변성 보장
    public static MissingCase reportBy(MissingPerson missingPerson, User reportedBy) {
        if (missingPerson == null) {
            throw new IllegalArgumentException("실종자 정보는 필수입니다.");
        }
        if (reportedBy == null) {
            throw new IllegalArgumentException("신고자 정보는 필수입니다.");
        }
        
        return MissingCase.builder()
                .missingPerson(missingPerson)
                .reportedBy(reportedBy)
                .caseStatus(CaseStatusType.OPEN)
                .build();
    }
    
    // 케이스 상태 변경
    // JPA Dirty Checking 활용
    public void changeStatus(CaseStatusType newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("케이스 상태는 필수입니다.");
        }
        this.caseStatus = newStatus;
    }

    // 케이스 종료
    // JPA Dirty Checking 활용
    public void close() {
        changeStatus(CaseStatusType.CLOSED);
    }

    // 등록자가 케이스를 종료합니다.
    // 등록자만 케이스를 종료할 수 있습니다.
    //
    // @param user 종료 요청자
    // @throws MissingPersonException 등록자가 아닌 경우 (UNAUTHORIZED_ACCESS)
    // @throws MissingPersonException 이미 종료된 케이스인 경우 (CASE_ALREADY_CLOSED)
    public void closeBy(User user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보는 필수입니다.");
        }
        if (!isReportedBy(user)) {
            throw new baro.baro.domain.missingperson.exception.MissingPersonException(
                    baro.baro.domain.missingperson.exception.MissingPersonErrorCode.UNAUTHORIZED_ACCESS);
        }
        if (this.caseStatus == CaseStatusType.CLOSED) {
            throw new baro.baro.domain.missingperson.exception.MissingPersonException(
                    baro.baro.domain.missingperson.exception.MissingPersonErrorCode.CASE_ALREADY_CLOSED);
        }
        close();
    }

    // 등록자 여부를 확인합니다.
    public boolean isReportedBy(User user) {
        return this.reportedBy.getId().equals(user.getId());
    }
}