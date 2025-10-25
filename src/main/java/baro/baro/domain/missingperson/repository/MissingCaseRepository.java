package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.MissingCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissingCaseRepository extends JpaRepository<MissingCase, Long> {
}
