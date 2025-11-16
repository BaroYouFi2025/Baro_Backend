package baro.baro.domain.auth.repository;

import baro.baro.domain.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

// 블랙리스트 토큰 리포지토리
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    // 토큰이 블랙리스트에 있는지 확인
    //
    // @param token 확인할 토큰
    // @return 블랙리스트에 있으면 true
    boolean existsByToken(String token);

    // 만료된 블랙리스트 토큰을 삭제
    // 정기적으로 실행하여 DB 공간 확보
    //
    // @param now 현재 시간
    // @return 삭제된 토큰 수
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    // 특정 사용자의 모든 토큰을 블랙리스트에서 삭제
    // (사용자 삭제 시 등)
    //
    // @param userId 사용자 ID
    // @return 삭제된 토큰 수
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
