package baro.baro.domain.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String base64Secret, //Base64로 인코딩된 비밀 키 문자열
            @Value("${jwt.access-validity-seconds:900}") long accessSeconds, //Access Token 유효시간(초 단위 : 60분)
            @Value("${jwt.refresh-validity-seconds:1209600}") long refreshSeconds //Refresh Token 유효시간(초단위 : 14일)
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTokenValidityMs = Duration.ofSeconds(accessSeconds).toMillis();
        this.refreshTokenValidityMs = Duration.ofSeconds(refreshSeconds).toMillis();
    }

    public String createAccessToken(String subject) {
        return buildToken(subject, accessTokenValidityMs);
    } //Access Token 생성

    public String createRefreshToken(String subject) {
        return buildToken(subject, refreshTokenValidityMs);
    } //Refresh Token 생성

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMs / 1000;
    } //Access Token 유효시간 반환

    private String buildToken(String subject, long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .setSubject(subject) //사용자 식별 값
                .setIssuedAt(now) //발급 시간
                .setExpiration(expiry) //만료 시간
                .signWith(key, SignatureAlgorithm.HS256) //키, 알고리즘으로 서명
                .compact(); //최종적으로 문자열 JWT 생성
    }
}
