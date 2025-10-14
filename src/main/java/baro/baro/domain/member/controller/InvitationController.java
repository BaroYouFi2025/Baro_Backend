package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.*;
import baro.baro.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<InvitationResponse> invitations(@RequestBody InvitationRequest request) {
        InvitationResponse response = memberService.makeInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/acceptance")
    public ResponseEntity<AcceptInvitationResponse> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
         AcceptInvitationResponse response = memberService.acceptInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/rejection")
    public  ResponseEntity<?> rejectInvitation(@RequestBody RejectInvitationRequest request) {
        memberService.rejectInvitation(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}