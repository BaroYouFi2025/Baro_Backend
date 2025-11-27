package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.req.LoginRequest;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;

public interface AuthService {
    AuthTokensResponse login(LoginRequest request);
    LogoutResponse logout(String refreshToken);
    RefreshResponse refresh(String refreshToken);
}


