package baro.baro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/**").permitAll() // ✅ API는 인증 필요 없음
                .anyRequest().authenticated()
                .and()
                .formLogin().disable(); // 필요 없으면 .disable() 해도 됨

        return http.build();
    }
}
