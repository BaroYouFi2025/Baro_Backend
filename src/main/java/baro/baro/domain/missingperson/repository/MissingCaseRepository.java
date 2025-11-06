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
    
    /**
     * 실종자와 케이스 상태로 실종 케이스를 조회합니다.
     *
     * @param missingPerson 실종자
     * @param caseStatus    케이스 상태
     * @return 실종 케이스
     */
    Optional<MissingCase> findByMissingPersonAndCaseStatus(MissingPerson missingPerson, CaseStatusType caseStatus);
}
