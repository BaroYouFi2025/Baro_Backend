package baro.baro.domain.auth.service;


import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;

public interface AuthService {
    AuthTokensResponse signup(SignupRequest request);
}


