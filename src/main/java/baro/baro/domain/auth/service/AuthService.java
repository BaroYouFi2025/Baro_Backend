package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.request.SignupRequest;
import baro.baro.domain.auth.dto.response.AuthTokensResponse;

public interface AuthService {
    AuthTokensResponse signup(SignupRequest request);
}


