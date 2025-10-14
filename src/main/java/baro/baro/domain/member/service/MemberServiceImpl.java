package baro.baro.domain.member.service;

import baro.baro.domain.member.dto.*;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.member.repository.InvitationRepository;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final InvitationRepository invitationRepository;

    @Override
    @Transactional // 구성원 초대 생성 메서드
    public InvitationResponse makeInvitation(InvitationRequest request) {
        User inviter = getCurrentUser();
        User invitee = userRepository.findById(request.getInviteeUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Invitation invitationRequest = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation(request.getRelation())
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Invitation created = invitationRepository.save(invitationRequest);
        return new InvitationResponse(created.getId());
    }

    @Override
    @Transactional // 구성원 초대 동의 메서드
    public AcceptInvitationResponse acceptInvitation(AcceptInvitationRequest request) {
        User invitee = getCurrentUser();

        Invitation invitation = invitationRepository.findById(request.getRelationshipRequestId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVITATION_NOT_FOUND));

        // 초대된 사용자가 맞는지 확인
        invitation.validateInvitee(invitee);

        // 초대 상태가 PENDING인지 확인(중복 수락 방지)
        // status가 PENDING이 아니면 예외 발생
        invitation.accept();
        invitationRepository.save(invitation);

        User inviter = invitation.getInviterUser();

        // 양방향 관계를 위해 두 개의 Relationship 엔티티 생성
        Relationship originRelationship = Relationship.builder()
                .user(inviter)
                .member(invitee)
                .relation(invitation.getRelation())
                .createdAt(LocalDateTime.now())
                .build();

        Relationship reverseRelationship = Relationship.builder()
                .user(invitee)
                .member(inviter)
                .relation(request.getRelation())
                .createdAt(LocalDateTime.now())
                .build();


        originRelationship = relationshipRepository.save(originRelationship);
        reverseRelationship = relationshipRepository.save(reverseRelationship);
        return AcceptInvitationResponse.of(originRelationship.getId(), reverseRelationship.getId());
    }

    @Override
    @Transactional // 구성원 초대 거절 메서드
    public void rejectInvitation(RejectInvitationRequest request) {
        User invitee = getCurrentUser();

        Invitation invitation = invitationRepository.findById(request.getRelationshipId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVITATION_NOT_FOUND));

        // 초대된 사용자가 맞는지 확인
        invitation.validateInvitee(invitee);

        // 초대 상태가 PENDING인지 확인(중복 거절 방지)
        invitation.reject();
        invitationRepository.save(invitation);
    }
}
