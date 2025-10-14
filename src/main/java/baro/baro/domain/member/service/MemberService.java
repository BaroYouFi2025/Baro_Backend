package baro.baro.domain.member.service;


import baro.baro.domain.member.dto.*;

public interface MemberService {
    InvitationResponse makeInvitation(InvitationRequest request);
    AcceptInvitationResponse acceptInvitation(AcceptInvitationRequest request);

    void rejectInvitation(RejectInvitationRequest request);
}
