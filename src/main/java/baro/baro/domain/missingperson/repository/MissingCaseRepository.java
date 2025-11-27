package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MissingCaseRepository extends JpaRepository<MissingCase, Long> {
    Optional<MissingCase> findByMissingPerson(MissingPerson missingPerson);

    @Query("SELECT m FROM MissingCase m WHERE m.missingPerson.id = :missingPersonId and m.caseStatus = 'OPEN'")
    Optional<MissingCase> findByMissingPersonId(Long missingPersonId);

    @Query("SELECT COUNT(m) FROM MissingCase m WHERE m.reportedBy.id = :userId and m.caseStatus = :caseStatusType")
    long countByReportedById(Long userId, CaseStatusType caseStatusType);

    // 비관적 락을 사용한 카운트 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(m) FROM MissingCase m WHERE m.reportedBy = :user and m.caseStatus = :caseStatusType")
    long countByReportedByAndCaseStatusWithLock(@Param("user") User user, @Param("caseStatusType") CaseStatusType caseStatusType);

    Optional<MissingCase> findByMissingPersonAndCaseStatus(MissingPerson missingPerson, CaseStatusType caseStatusType);
}
