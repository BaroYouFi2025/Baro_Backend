package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MissingPersonPoliceRepository extends JpaRepository<MissingPersonPolice, Long> {

    // 모든 실종자 ID 목록 조회 (Batch Upsert 최적화용)
    @Query("SELECT m.id FROM MissingPersonPolice m")
    Set<Long> findAllIds();

}
