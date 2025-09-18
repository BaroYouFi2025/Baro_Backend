package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.request.SignupRequest;
import baro.baro.domain.auth.dto.response.AuthTokensResponse;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthTokensResponse signup(SignupRequest request) {
        User user = userService.createUser(
                request.getUid(),
                request.getPassword(),
                request.getPhone(),
                request.getUsername(),
                request.getBirthDate()
        );

        String access = jwtTokenProvider.createAccessToken(user.getUid());
        String refresh = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        return new AuthTokensResponse(access, refresh, expiresIn);
    }
}
