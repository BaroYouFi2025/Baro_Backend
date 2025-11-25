package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.Sighting;
import baro.baro.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

// 실종자 목격/발견 신고 Repository
@Repository
public interface SightingRepository extends JpaRepository<Sighting, Long> {
    
    // 특정 실종 케이스의 모든 목격 신고를 조회합니다.
    //
    // @param missingCase 실종 케이스
    // @return 목격 신고 리스트
    List<Sighting> findByMissingCase(MissingCase missingCase);
    
    // 특정 실종 케이스의 목격 신고 개수를 조회합니다.
    //
    // @param missingCase 실종 케이스
    // @return 목격 신고 개수
    long countByMissingCase(MissingCase missingCase);
    
    // 특정 실종 케이스에서 최근 목격 신고를 조회합니다.
    //
    // @param missingCase 실종 케이스
    // @return 최근 목격 신고
    Optional<Sighting> findFirstByMissingCaseOrderByCreatedAtDesc(MissingCase missingCase);
    
    // 특정 실종 케이스 ID로 모든 목격 신고를 조회합니다.
    //
    // @param missingCaseId 실종 케이스 ID
    // @return 목격 신고 리스트
    @Query("SELECT s FROM Sighting s WHERE s.missingCase.id = :missingCaseId ORDER BY s.createdAt DESC")
    List<Sighting> findByMissingCaseId(@Param("missingCaseId") Long missingCaseId);
    
    // 특정 사용자가 특정 실종 케이스에 대해 특정 시간 이후에 신고한 이력이 있는지 확인합니다.
    // 중복 신고 방지를 위해 사용됩니다.
    //
    // @param missingCase 실종 케이스
    // @param reporter    신고자
    // @param since       조회 시작 시간 (이 시간 이후의 신고만 조회)
    // @return 해당 기간 내 신고 이력
    @Query("SELECT s FROM Sighting s WHERE s.missingCase = :missingCase " +
           "AND s.reporter = :reporter " +
           "AND s.createdAt > :since " +
           "ORDER BY s.createdAt DESC")
    List<Sighting> findRecentSightingsByReporter(
            @Param("missingCase") MissingCase missingCase,
            @Param("reporter") User reporter,
            @Param("since") ZonedDateTime since
    );
    
    // 특정 사용자가 특정 실종 케이스에 대해 특정 시간 이후에 신고한 이력이 있는지 확인합니다.
    //
    // @param missingCase 실종 케이스
    // @param reporter    신고자
    // @param since       조회 시작 시간
    // @return 신고 이력이 있으면 true
    default boolean existsRecentSightingByReporter(
            MissingCase missingCase,
            User reporter,
            ZonedDateTime since) {
        return !findRecentSightingsByReporter(missingCase, reporter, since).isEmpty();
    }

    // 비관적 락을 사용한 중복 신고 확인 (동시성 제어)
    // 10분 이내 같은 실종 케이스에 대한 같은 신고자의 신고가 있는지 확인
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(s) > 0 FROM Sighting s " +
           "WHERE s.missingCase = :missingCase " +
           "AND s.reporter = :reporter " +
           "AND s.createdAt > :since")
    boolean existsRecentSightingWithLock(
            @Param("missingCase") MissingCase missingCase,
            @Param("reporter") User reporter,
            @Param("since") ZonedDateTime since
    );
}

