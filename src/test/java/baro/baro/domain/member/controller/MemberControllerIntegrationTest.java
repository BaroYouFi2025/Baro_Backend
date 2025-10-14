package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.*;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.member.repository.InvitationRepository;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private RelationshipRepository relationshipRepository;

    private User inviter;
    private User invitee;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        relationshipRepository.deleteAll();
        invitationRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자를 실제 DB에 insert
        inviter = User.builder()
                .uid("inviter_uid_" + System.currentTimeMillis())
                .passwordHash("password123")
                .phoneE164("+821012345678")
                .name("초대자")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        inviter = userRepository.saveAndFlush(inviter);

        invitee = User.builder()
                .uid("invitee_uid_" + System.currentTimeMillis())
                .passwordHash("password456")
                .phoneE164("+821087654321")
                .name("피초대자")
                .birthDate(LocalDate.of(1995, 5, 5))
                .build();
        invitee = userRepository.saveAndFlush(invitee);

        // SecurityContext에 inviter를 현재 사용자로 설정
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(inviter.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("초대 생성 통합 테스트 - 성공")
    void createInvitation_Integration_Success() throws Exception {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(invitee.getId());
        request.setRelation("가족");

        // when & then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipRequestId").exists());

        // 실제 DB에 저장되었는지 확인
        Invitation savedInvitation = invitationRepository.findAll().get(0);
        assertThat(savedInvitation).isNotNull();
        assertThat(savedInvitation.getInviterUser().getId()).isEqualTo(inviter.getId());
        assertThat(savedInvitation.getInviteeUser().getId()).isEqualTo(invitee.getId());
        assertThat(savedInvitation.getRelation()).isEqualTo("가족");
        assertThat(savedInvitation.getStatus()).isEqualTo(RelationshipRequestStatus.PENDING);
    }

    @Test
    @DisplayName("초대 생성 통합 테스트 - 존재하지 않는 사용자")
    void createInvitation_Integration_UserNotFound() throws Exception {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(99999L); // 존재하지 않는 ID
        request.setRelation("가족");

        // when & then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());

        // DB에 저장되지 않았는지 확인
        assertThat(invitationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("초대 수락 통합 테스트 - 성공")
    void acceptInvitation_Integration_Success() throws Exception {
        // given - 초대를 DB에 insert
        Invitation invitation = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        invitation = invitationRepository.saveAndFlush(invitation);

        // SecurityContext를 invitee로 변경 (초대를 수락하는 사람)
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(invitee.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(invitation.getId());
        request.setRelation("친구");

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipIds").isArray())
                .andExpect(jsonPath("$.relationshipIds.length()").value(2));

        // 실제 DB에 관계가 생성되었는지 확인
        assertThat(relationshipRepository.findAll()).hasSize(2);
        
        // 초대 상태가 ACCEPTED로 변경되었는지 확인
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertThat(updatedInvitation.getStatus()).isEqualTo(RelationshipRequestStatus.ACCEPTED);
        assertThat(updatedInvitation.getRespondedAt()).isNotNull();
    }

    @Test
    @DisplayName("초대 수락 통합 테스트 - 존재하지 않는 초대")
    void acceptInvitation_Integration_InvitationNotFound() throws Exception {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(99999L); // 존재하지 않는 ID
        request.setRelation("친구");

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());

        // 관계가 생성되지 않았는지 확인
        assertThat(relationshipRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("초대 수락 통합 테스트 - 권한 없음 (다른 사용자가 수락 시도)")
    void acceptInvitation_Integration_NotCorrectInvitee() throws Exception {
        // given - 초대를 DB에 insert
        Invitation invitation = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        invitation = invitationRepository.saveAndFlush(invitation);

        // inviter가 자신의 초대를 수락하려고 시도 (잘못된 시도)
        // SecurityContext는 setUp에서 inviter로 설정됨

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(invitation.getId());
        request.setRelation("친구");

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        // 관계가 생성되지 않았는지 확인
        assertThat(relationshipRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("초대 수락 통합 테스트 - 이미 수락된 초대")
    void acceptInvitation_Integration_AlreadyAccepted() throws Exception {
        // given - 이미 수락된 초대를 DB에 insert
        Invitation invitation = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.ACCEPTED)
                .createdAt(java.time.LocalDateTime.now())
                .respondedAt(java.time.LocalDateTime.now())
                .build();
        invitation = invitationRepository.saveAndFlush(invitation);

        // SecurityContext를 invitee로 변경
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(invitee.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(invitation.getId());
        request.setRelation("친구");

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // 중복 관계가 생성되지 않았는지 확인
        assertThat(relationshipRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("초대 거절 통합 테스트 - 성공")
    void rejectInvitation_Integration_Success() throws Exception {
        // given - 초대를 DB에 insert
        Invitation invitation = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        invitation = invitationRepository.saveAndFlush(invitation);

        // SecurityContext를 invitee로 변경 (초대를 거절하는 사람)
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(invitee.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(invitation.getId());

        // when & then
        mockMvc.perform(delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 초대 상태가 REJECTED로 변경되었는지 확인
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertThat(updatedInvitation.getStatus()).isEqualTo(RelationshipRequestStatus.REJECTED);
        assertThat(updatedInvitation.getRespondedAt()).isNotNull();
    }

    @Test
    @DisplayName("초대 거절 통합 테스트 - 존재하지 않는 초대")
    void rejectInvitation_Integration_InvitationNotFound() throws Exception {
        // given
        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(99999L); // 존재하지 않는 ID

        // when & then
        mockMvc.perform(delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("초대 거절 통합 테스트 - 권한 없음 (다른 사용자가 거절 시도)")
    void rejectInvitation_Integration_NotCorrectInvitee() throws Exception {
        // given - 초대를 DB에 insert
        Invitation invitation = Invitation.builder()
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        invitation = invitationRepository.saveAndFlush(invitation);

        // inviter가 자신의 초대를 거절하려고 시도 (잘못된 시도)
        // SecurityContext는 setUp에서 inviter로 설정됨

        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(invitation.getId());

        // when & then
        mockMvc.perform(delete("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        // 초대 상태가 변경되지 않았는지 확인
        Invitation unchangedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertThat(unchangedInvitation.getStatus()).isEqualTo(RelationshipRequestStatus.PENDING);
    }
}
