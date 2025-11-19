package baro.baro.domain.member.service;


import baro.baro.domain.member.dto.response.*;
import baro.baro.domain.member.dto.request.*;

import java.util.List;

public interface MemberService {
    InvitationResponse makeInvitation(InvitationRequest request);
    AcceptInvitationResponse acceptInvitation(AcceptInvitationRequest request);

    void rejectInvitation(RejectInvitationRequest request);
    List<MemberLocationResponse> getMemberLocations();

    // 특정 사용자의 관점에서 구성원 위치 목록을 조회합니다.
    // SSE 브로드캐스트 시 사용됩니다.
    List<MemberLocationResponse> getMemberLocationsForUser(Long userId);
}
