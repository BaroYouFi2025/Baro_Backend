package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthTokensResponse login(LoginRequest request, HttpServletResponse response);
    LogoutResponse logout(LogoutRequest request, HttpServletResponse response);
    RefreshResponse refresh(RefreshRequest request);
}


