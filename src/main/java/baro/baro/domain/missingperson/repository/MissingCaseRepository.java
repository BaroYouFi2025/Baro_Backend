package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MissingCaseRepository extends JpaRepository<MissingCase, Long> {

    @Query("SELECT m FROM MissingCase m WHERE m.missingPerson.id = :missingPersonId and m.caseStatus = 'OPEN'")
    Optional<MissingCase> findByMissingPersonId(Long missingPersonId);

    @Query("SELECT COUNT(m) FROM MissingCase m WHERE m.reportedBy = :userId and m.caseStatus = 'OPEN'")
    long countByReportedById(Long userId);

    Optional<MissingCase> findByMissingPersonAndCaseStatus(MissingPerson missingPerson, CaseStatusType caseStatusType);
}
