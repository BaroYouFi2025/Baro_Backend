package baro.baro.domain.member.service;


import baro.baro.domain.member.dto.response.*;
import baro.baro.domain.member.dto.request.*;
import java.util.List;

public interface MemberService {
    InvitationResponse makeInvitation(InvitationRequest request);
    AcceptInvitationResponse acceptInvitation(AcceptInvitationRequest request);

    void rejectInvitation(RejectInvitationRequest request);
    List<MemberResponse> getMember();
    List<MemberLocationResponse> getMemberLocations();
}
