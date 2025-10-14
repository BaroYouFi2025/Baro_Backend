package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.*;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/invitations")
    public ResponseEntity<InvitationResponse> invitations(@RequestBody InvitationRequest request) {
        InvitationResponse response = memberService.makeInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<AcceptInvitationResponse> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
         AcceptInvitationResponse response = memberService.acceptInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping
    public  ResponseEntity<?> rejectInvitation(@RequestBody RejectInvitationRequest request) {
        memberService.rejectInvitation(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}