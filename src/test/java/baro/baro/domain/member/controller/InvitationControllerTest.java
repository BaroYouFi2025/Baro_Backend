package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.*;
import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.member.service.MemberService;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InvitationController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@DisplayName("MemberController API 테스트")
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("POST /members/invitations - 초대 생성 성공")
    void createInvitation_Success() throws Exception {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(2L);

        InvitationResponse response = new InvitationResponse(1L);
        when(memberService.makeInvitation(any(InvitationRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipRequestId").value(1L));

        verify(memberService, times(1)).makeInvitation(any(InvitationRequest.class));
    }

    @Test
    @DisplayName("POST /members/invitations - 피초대자를 찾을 수 없음")
    void createInvitation_UserNotFound() throws Exception {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(999L);

        when(memberService.makeInvitation(any(InvitationRequest.class)))
                .thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /members - 초대 수락 성공")
    void acceptInvitation_Success() throws Exception {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(1L);
        request.setRelation("친구");

        AcceptInvitationResponse response = AcceptInvitationResponse.of(1L, 2L);
        when(memberService.acceptInvitation(any(AcceptInvitationRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipIds").isArray())
                .andExpect(jsonPath("$.relationshipIds.length()").value(2))
                .andExpect(jsonPath("$.relationshipIds[0]").value(1L))
                .andExpect(jsonPath("$.relationshipIds[1]").value(2L));

        verify(memberService, times(1)).acceptInvitation(any(AcceptInvitationRequest.class));
    }

    @Test
    @DisplayName("POST /members - 초대를 찾을 수 없음")
    void acceptInvitation_InvitationNotFound() throws Exception {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(999L);
        request.setRelation("친구");

        when(memberService.acceptInvitation(any(AcceptInvitationRequest.class)))
                .thenThrow(new MemberException(MemberErrorCode.INVITATION_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /members - 초대 거절 성공")
    void rejectInvitation_Success() throws Exception {
        // given
        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(1L);

        doNothing().when(memberService).rejectInvitation(any(RejectInvitationRequest.class));

        // when & then
        mockMvc.perform(delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).rejectInvitation(any(RejectInvitationRequest.class));
    }

    @Test
    @DisplayName("DELETE /members - 초대를 찾을 수 없음")
    void rejectInvitation_InvitationNotFound() throws Exception {
        // given
        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(999L);

        doThrow(new MemberException(MemberErrorCode.INVITATION_NOT_FOUND))
                .when(memberService).rejectInvitation(any(RejectInvitationRequest.class));

        // when & then
        mockMvc.perform(delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
