package baro.baro.domain.member.repository;

import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    // 두 사용자 간 대기 중인 초대가 있는지 확인 (양방향)
    Optional<Invitation> findByInviterUserAndInviteeUserAndStatus(
            User inviterUser,
            User inviteeUser,
            RelationshipRequestStatus status
    );

    // 역방향 초대도 확인 (invitee가 inviter에게 초대한 경우)
    Optional<Invitation> findByInviterUserAndInviteeUserAndStatusOrInviterUserAndInviteeUserAndStatus(
            User inviterUser1,
            User inviteeUser1,
            RelationshipRequestStatus status1,
            User inviterUser2,
            User inviteeUser2,
            RelationshipRequestStatus status2
    );
}
