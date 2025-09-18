package baro.baro.domain.user.service;

import baro.baro.domain.user.entity.request.SignupRequest;
import baro.baro.domain.user.entity.response.AuthTokensResponse;

public interface AuthService {
    AuthTokensResponse signup(SignupRequest request);
}


