package baro.baro.domain.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Slf4j
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

    public String createAccessToken(String subject, String role, Long deviceId) {
        return buildTokenWithClaims(subject, role, deviceId, accessTokenValidityMs);
    } //Access Token 생성

    public String createRefreshToken(String subject) {
        return buildToken(subject, refreshTokenValidityMs);
    } //Refresh Token 생성

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMs / 1000;
    }

    // Refresh Token의 유효 시간(밀리초)을 반환합니다.
    //
    // @return Refresh Token 유효 시간 (밀리초)
    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }

    // JWT 토큰의 유효성을 검증합니다.
    //
    // @param token 검증할 JWT 토큰
    // @return 유효한 토큰이면 true, 그렇지 않으면 false
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다. Token: {}", token.substring(0, Math.min(20, token.length())));
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. Expired at: {}", e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다. Error: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT 토큰 검증 중 JWT 오류 발생: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * JWT 토큰에서 subject를 추출합니다.
     * 만료된 토큰도 subject 추출이 가능합니다 (블랙리스트 등록 등을 위해).
     *
     * @param token JWT 토큰
     * @return subject (사용자 UID)
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public String getSubjectFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰도 subject 추출 가능 (블랙리스트 등록, 로그아웃 등을 위해)
            log.debug("만료된 토큰에서 subject 추출: {}", e.getClaims().getSubject());
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            log.error("토큰에서 subject 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

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

    private String buildTokenWithClaims(String subject, String role, Long deviceId, long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        var builder = Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);

        if (deviceId != null) {
            builder.claim("deviceId", deviceId);
        }

        return builder.compact();
    }

    // JWT 토큰에서 role을 추출합니다.
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("role", String.class);
        }
    }

    // JWT 토큰에서 deviceId를 추출합니다.
    public Long getDeviceIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("deviceId", Long.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("deviceId", Long.class);
        }
    }
}
