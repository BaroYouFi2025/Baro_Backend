package baro.baro.domain.user.service;

import baro.baro.domain.user.entity.User;

public interface UserService {
    User createUser(String uid, String rawPassword, String phone, String nickname, String birthDateIso);
}
