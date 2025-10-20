package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    User createUser(String uid, String rawPassword, String phone, String name, String birthDateIso);
    AuthTokensResponse signup(SignupRequest request, HttpServletResponse response);
}
