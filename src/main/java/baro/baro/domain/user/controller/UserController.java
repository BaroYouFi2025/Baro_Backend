package baro.baro.domain.user.controller;

import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.res.AuthTokensResponse;
import baro.baro.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthTokensResponse tokens = userService.signup(request);
        return ResponseEntity.ok(tokens);
    }
}
