package baro.baro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정: JWT 기반 인증에서는 CSRF 보호 비활성화
                // (Stateless 방식이므로 CSRF 토큰 불필요, 대신 JWT 자체가 보안 역할)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리: JWT 사용으로 세션 불필요 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 인증 규칙 설정
                .authorizeHttpRequests(auth -> auth
                        // Auth Controller - 인증 불필요
                        .requestMatchers(HttpMethod.POST, "/auth/phone/verifications").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/phone/verifications/test").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/phone/verifications").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                        // User Controller - 회원가입은 인증 불필요
                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()

                        // Member Controller - 초대 관련 엔드포인트 (인증 불필요)
                        .requestMatchers(HttpMethod.POST, "/members/invitations/acceptance").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/members/invitations/rejection").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/invitations").permitAll()

                        // MissingPerson Controller - 공개 조회 엔드포인트
                        .requestMatchers(HttpMethod.GET, "/missing-person/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/missing-persons/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/missing-persons/{id}").permitAll()

                        // Police API Controller - 경찰청 공개 데이터 조회
                        .requestMatchers(HttpMethod.GET, "/missing/police/missing-persons").permitAll()
                        .requestMatchers(HttpMethod.GET, "/missing/police/missing-persons/{id}").permitAll()

                        // Location Controller - 좌표 변환 유틸리티
                        .requestMatchers(HttpMethod.GET, "/location/address").permitAll()

                        // Police office 데이터 - 누구나 조회 가능
                        .requestMatchers(HttpMethod.GET, "/police-offices/nearby").permitAll()

                        // Swagger UI - 개발 환경에서만 허용 권장
                        // 프로덕션에서는 제거하거나 인증 적용 필요
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Actoruator 엔드포인트
                        .requestMatchers("/actuator/**").permitAll()

                        // 정적 리소스 - 업로드된 이미지 접근 허용
                        .requestMatchers("/images/**").permitAll()

                        // Notification Controller
                        .requestMatchers(HttpMethod.GET, "/notifications/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notifications/me/unread").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notifications/me/unread/count").permitAll()
                        .requestMatchers(HttpMethod.POST, "/notifications/{notificationId}/accept-invitation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/notifications/{notificationId}/reject-invitation").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notifications/{notificationId}/missing-person").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notifications/{notificationId}/sighting").permitAll()

                        // 기본 정책: 명시되지 않은 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Form 로그인 비활성화 (JWT 사용)
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // CORS 설정
    // 프론트엔드 애플리케이션에서 백엔드 API를 호출할 수 있도록 CORS 정책 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정 (프론트엔드 URL)
        // 프로덕션에서는 실제 프론트엔드 도메인으로 변경 필요
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 여부 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        // 노출할 헤더 (프론트엔드에서 접근 가능한 헤더)
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
