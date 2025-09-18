package baro.baro.domain.user.controller;

import baro.baro.domain.user.entity.request.SignupRequest;
import baro.baro.domain.user.entity.response.AuthTokensResponse;
import baro.baro.domain.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthTokensResponse tokens = authService.signup(request);
        return ResponseEntity.ok(tokens);
    }
}


