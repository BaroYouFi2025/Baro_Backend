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

@Configuration
@RequiredArgsConstructor
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
                
                // 인증 규칙 설정
                .authorizeHttpRequests(auth -> auth
                        // Auth Controller - 인증 불필요
                        .requestMatchers(HttpMethod.POST, "/auth/phone/verifications").permitAll()
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

                        // Swagger UI - 개발 환경에서만 허용 권장
                        // 프로덕션에서는 제거하거나 인증 적용 필요
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // 기본 정책: 명시되지 않은 모든 요청은 인증 필요
                        // (보안 강화: permitAll() 대신 authenticated() 사용)
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
}
