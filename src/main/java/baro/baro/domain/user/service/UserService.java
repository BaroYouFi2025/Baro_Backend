package baro.baro.domain.user.service;

import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.dto.res.AuthTokensResponse;

public interface UserService {
    User createUser(String uid, String rawPassword, String phone, String name, String birthDateIso);
    AuthTokensResponse signup(SignupRequest request);
}
