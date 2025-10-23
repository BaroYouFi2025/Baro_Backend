package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import baro.baro.domain.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    User createUser(String uid, String rawPassword, String phone, String name, String birthDateIso);
    AuthTokensResponse signup(SignupRequest request, HttpServletResponse response);
    UserProfileResponse getProfile();
    UserProfileResponse updateProfile(UpdateProfileRequest request);
    DeleteUserResponse deleteUser(DeleteUserRequest request);
}
