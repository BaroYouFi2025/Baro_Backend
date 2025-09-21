package baro.baro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Auth Controller
                        .requestMatchers(HttpMethod.POST, "/auth/phone/verifications").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/phone/verifications").permitAll()

                        // User Controller
                        .requestMatchers(HttpMethod.POST , "/users/register").permitAll()

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable()); // 필요 없으면 disable
        return http.build();
    }
}
