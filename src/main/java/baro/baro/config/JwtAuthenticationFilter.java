package baro.baro.config;

import baro.baro.domain.auth.service.JwtTokenProvider;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// JWT 인증 필터
// 모든 HTTP 요청에서 JWT 토큰을 검증하고 인증 정보를 설정합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 요청에서 JWT 토큰 추출
            String jwt = extractJwtFromRequest(request);

            // 토큰이 있고 유효한 경우 인증 설정
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 토큰에서 사용자 ID 추출
                String userId = jwtTokenProvider.getSubjectFromToken(jwt);

                // 사용자가 활성 상태인지 확인
                User user = userRepository.findByUid(userId).orElse(null);
                if (user == null || !user.isActive()) {
                    log.warn("비활성화된 사용자의 요청 차단 - User ID: {}, URI: {}", userId, request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // 인증 객체 생성 (User 객체를 principal로 저장하여 이후 DB 재조회 방지)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,  // User 객체를 principal로 저장
                                null,
                                Collections.emptyList()
                        );

                // 요청 상세 정보 설정
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 - User ID: {}, URI: {}", userId, request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
        // 예외가 발생해도 필터 체인은 계속 진행 (인증 실패로 처리됨)
        }

        filterChain.doFilter(request, response);
    }

    // HTTP 요청에서 JWT 토큰을 추출합니다.
    // Authorization 헤더에서 Bearer 토큰을 찾습니다.
    //
    // @param request HTTP 요청
    // @return JWT 토큰 (없으면 null)
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
