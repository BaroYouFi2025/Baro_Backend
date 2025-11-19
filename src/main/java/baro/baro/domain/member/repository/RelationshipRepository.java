package baro.baro.domain.member.repository;

import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    // 특정 사용자의 모든 관계(구성원)를 조회합니다.
    //
    // @param user 조회할 사용자
    // @return 사용자의 관계 목록
    List<Relationship> findByUser(User user);

    // 특정 사용자의 모든 관계를 member 정보와 함께 조회합니다 (N+1 문제 해결).
    //
    // @param user 조회할 사용자
    // @return 사용자의 관계 목록 (member fetch join)
    @Query("SELECT r FROM Relationship r JOIN FETCH r.member WHERE r.user = :user")
    List<Relationship> findByUserWithMember(@Param("user") User user);

    // 특정 member와 관계를 맺은 모든 사용자의 ID를 조회합니다.
    // 위치 업데이트 시 브로드캐스트 대상을 찾는 데 사용됩니다.
    //
    // @param memberId 구성원 ID (위치가 변경된 사용자)
    // @return 해당 구성원과 관계를 맺은 사용자 ID 목록
    @Query("SELECT r.user.id FROM Relationship r WHERE r.member.id = :memberId")
    List<Long> findUserIdsByMemberId(@Param("memberId") Long memberId);
}
