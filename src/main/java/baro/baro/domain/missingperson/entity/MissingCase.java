package baro.baro.domain.missingperson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.common.enums.CaseStatusType;
import baro.baro.domain.user.entity.User;

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
    @Builder.Default
    private CaseStatusType caseStatus = CaseStatusType.OPEN;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;
    
    @CreationTimestamp
    @Column(name = "reported_at", nullable = false)
    private ZonedDateTime reportedAt;
}