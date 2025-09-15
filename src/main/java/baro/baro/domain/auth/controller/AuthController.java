package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.request.SignupRequest;
import baro.baro.domain.auth.dto.response.AuthTokensResponse;
import baro.baro.domain.auth.service.AuthService;
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


