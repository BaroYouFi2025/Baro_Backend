package baro.baro.domain.member.entity;

import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static baro.baro.domain.member.exception.MemberErrorCode.NOT_CORRECT_INVITEE;

@Table(name = "invitations", schema = "youfi")
@Entity
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class Invitation {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_user_id")
    private User inviterUser;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_user_id")
    private User inviteeUser;

    @Getter
    @Column(name = "relation")
    private String relation; // inviterUser와 inviteeUser의 관계 (예: 가족, 친구 등)

    @Getter
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RelationshipRequestStatus status; // PENDING, ACCEPTED, REJECTED

    @Getter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // 초대된 사용자가 맞는지 검증
    public void validateInvitee(User invitee) {
        if (!this.inviteeUser.equals(invitee)) {
            throw new MemberException(NOT_CORRECT_INVITEE);
        }
    }

    // 상태가 PENDING인지 검증
    private void validatePendingStatus() {
        if (this.status != RelationshipRequestStatus.PENDING) {
            throw new MemberException(MemberErrorCode.STATUS_IS_NOT_PENDING);
        }
    }

    // 초대 수락
    public void accept() {
        validatePendingStatus();
        this.status = RelationshipRequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        validatePendingStatus();
        this.status = RelationshipRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }
}
