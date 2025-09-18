package baro.baro.domain.user.service;

import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.res.AuthTokensResponse;

public interface AuthService {
    AuthTokensResponse signup(SignupRequest request);
}


