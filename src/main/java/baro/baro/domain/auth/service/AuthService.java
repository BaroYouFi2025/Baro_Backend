package baro.baro.domain.auth.service;


import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;

public interface AuthService {
    AuthTokensResponse login(LoginRequest request);
    LogoutResponse logout(LogoutRequest request);
    RefreshResponse refresh(RefreshRequest request);
}


