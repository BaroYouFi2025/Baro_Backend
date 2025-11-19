# Baro Backend 코드 리뷰 보고서

**분석 대상**: develop 브랜치
**분석 일자**: 2025-11-19
**분석자**: Claude Code Review

---

## 목차

1. [개요](#개요)
2. [심각도 1: 치명적 문제](#-심각도-1-치명적-문제-즉시-수정-필요)
3. [심각도 2: 주요 문제](#-심각도-2-주요-문제-조속한-수정-필요)
4. [심각도 3: 설계/아키텍처 문제](#-심각도-3-설계아키텍처-문제)
5. [심각도 4: 개선 권장 사항](#-심각도-4-개선-권장-사항)
6. [요약 테이블](#요약-테이블)

---

## 개요

본 보고서는 Baro Backend 프로젝트의 develop 브랜치에 대한 코드 리뷰 결과입니다. 총 **18개의 주요 문제점**이 발견되었으며, 보안, 동시성, 비즈니스 로직 오류 등 다양한 카테고리의 이슈가 포함되어 있습니다.

### 분석 범위

- Config 설정 (Security, JWT, Firebase, Async 등)
- AI 이미지 생성 도메인
- 인증/인가 도메인
- Device/GPS 추적 도메인
- MissingPerson 도메인
- Notification 도메인

---

## 🔴 심각도 1: 치명적 문제 (즉시 수정 필요)

### 1. 보안 취약점 - 인증 필요 엔드포인트가 permitAll() 설정됨

**파일**: `src/main/java/baro/baro/config/SecurityConfig.java:89-96`

```java
// Notification Controller
.requestMatchers(HttpMethod.GET, "/notifications/me").permitAll()
.requestMatchers(HttpMethod.GET, "/notifications/me/unread").permitAll()
.requestMatchers(HttpMethod.POST, "/notifications/{notificationId}/accept-invitation").permitAll()
.requestMatchers(HttpMethod.POST, "/notifications/{notificationId}/reject-invitation").permitAll()
```

**문제점**:
- 사용자 알림은 민감한 개인 정보인데 인증 없이 접근 가능
- `/notifications/me` - 사용자의 모든 알림 노출
- 초대 수락/거부 - 인증 없이 타인의 초대 조작 가능

**영향도**: 개인정보 유출, 권한 탈취

**수정안**:
해당 엔드포인트들을 `.authenticated()` 규칙으로 이동하거나, `anyRequest().authenticated()` 규칙에 의해 보호되도록 permitAll 설정 제거

---

### 2. 트랜잭션 전파 문제 - @Async와 @Transactional 충돌

**파일**: `src/main/java/baro/baro/domain/device/service/DeviceServiceImpl.java:228-230`

```java
@Async
@Transactional
public void checkNearbyMissingPersons(User user, Point location) {
```

**문제점**:
- `@Async` 메서드는 별도 스레드에서 실행되어 호출자의 트랜잭션 컨텍스트를 공유하지 않음
- `updateGps()` 메서드에서 호출 시 이미 트랜잭션이 커밋된 후 실행될 수 있음
- 결과적으로 데이터 불일치나 `LazyInitializationException` 발생 가능

**영향도**: 데이터 불일치, 런타임 예외

**수정안**:
```java
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void checkNearbyMissingPersons(Long userId, double latitude, double longitude) {
    // user 객체를 다시 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
    location.setSRID(4326);

    // ... 나머지 로직
}
```

---

### 3. 중복 import 및 잠재적 컴파일 경고

**파일**: `src/main/java/baro/baro/domain/missingperson/service/MissingPersonServiceImpl.java:11-17`

```java
import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.Sighting;
...
import baro.baro.domain.missingperson.entity.CaseStatusType;  // 중복
import baro.baro.domain.missingperson.entity.Sighting;  // 중복
```

**문제점**: 중복 import로 인한 코드 품질 저하

**수정안**: 중복된 import 문 제거

---

### 4. Rate Limiter 동시성 문제

**파일**: `src/main/java/baro/baro/domain/ai/service/RateLimiter.java:24, 104-111`

```java
public synchronized boolean tryAcquire(int rpm, int rpd) { ... }

// 하지만 이 메서드들은 synchronized가 아님
public int getCurrentRpm() {
    return requestTimestamps.size();  // lock-free 접근
}

public int getCurrentRpd() {
    return dailyRequestTimestamps.size();
}
```

**문제점**:
- `tryAcquire()`는 synchronized지만 모니터링 메서드들은 그렇지 않음
- `ConcurrentLinkedQueue`의 `size()`는 O(n) 연산이며 정확하지 않을 수 있음
- `tryAcquire()` 내부에서 cleanup 후 size 체크 사이에 race condition 가능

**영향도**: Race condition, 정확하지 않은 Rate limiting

**수정안**:
```java
private final AtomicInteger rpmCounter = new AtomicInteger(0);
private final AtomicInteger rpdCounter = new AtomicInteger(0);

public synchronized boolean tryAcquire(int rpm, int rpd) {
    // cleanup 시 카운터도 감소
    // ...
    rpmCounter.incrementAndGet();
    rpdCounter.incrementAndGet();
    return true;
}

public int getCurrentRpm() {
    return rpmCounter.get();
}
```

---

## 🟠 심각도 2: 주요 문제 (조속한 수정 필요)

### 5. 나이 계산 로직 오류

**파일**: `src/main/java/baro/baro/domain/missingperson/entity/MissingPerson.java:95-107`

```java
public Integer getAge() {
    if (birthDate == null) return null;
    return LocalDate.now().getYear() - birthDate.getYear();  // 잘못됨
}

public Integer getMissingAge(){
    if (birthDate == null || missingDate == null) return null;
    return missingDate.getYear() - birthDate.getYear();  // 잘못됨
}
```

**문제점**: 생일이 지났는지 확인하지 않아 최대 1년 오차 발생

**예시**:
- 생년월일: 2000-12-31
- 현재 날짜: 2024-01-01
- 잘못된 결과: 24세 (실제: 23세)

**수정안**:
```java
import java.time.Period;

public Integer getAge() {
    if (birthDate == null) return null;
    return Period.between(birthDate, LocalDate.now()).getYears();
}

public Integer getMissingAge() {
    if (birthDate == null || missingDate == null) return null;
    return Period.between(birthDate, missingDate.toLocalDate()).getYears();
}
```

---

### 6. 토큰 블랙리스트 만료 시간 계산 오류

**파일**: `src/main/java/baro/baro/domain/auth/service/AuthServiceImpl.java:86-87, 126-127`

```java
long validityMs = jwtTokenProvider.getRefreshTokenValidityMs();
LocalDateTime expiresAt = LocalDateTime.now().plusNanos(validityMs * 1_000_000);
```

**문제점**:
- 밀리초를 나노초로 변환하는 의도였으나, 결과적으로 1000배 증가
- 1ms = 1,000,000ns 이므로, `validityMs * 1_000_000`은 ms를 ns로 변환하는 것이 맞지만
- `plusNanos()`에 전달할 때 오버플로우 가능성 있음

**수정안**:
```java
import java.time.temporal.ChronoUnit;

LocalDateTime expiresAt = LocalDateTime.now()
    .plus(validityMs, ChronoUnit.MILLIS);
```

---

### 7. CompletableFuture 예외 처리 및 스레드풀 문제

**파일**: `src/main/java/baro/baro/domain/ai/service/GoogleGenAiService.java:106-114`

```java
for (int i = 0; i < 4; i++) {
    final int sequenceOrder = i;
    CompletableFuture<String> future =
        CompletableFuture.supplyAsync(() -> {
            return generateImageWithPhoto(...);
        });  // 기본 ForkJoinPool 사용
    futures.add(future);
}
```

**문제점**:
- 기본 `ForkJoinPool.commonPool()` 사용 시 애플리케이션 전체 성능에 영향
- 블로킹 I/O 작업에 적합하지 않음
- 예외 발생 시 `join()`에서 `CompletionException`으로 래핑됨

**수정안**:
```java
@Autowired
@Qualifier("aiImageExecutor")
private Executor aiImageExecutor;

CompletableFuture<String> future = CompletableFuture.supplyAsync(
    () -> generateImageWithPhoto(...),
    aiImageExecutor
);
```

---

### 8. 실종자 정보 수정 권한 검증 누락

**파일**: `src/main/java/baro/baro/domain/missingperson/service/MissingPersonServiceImpl.java:104`

```java
@Override
@Transactional
public RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request) {
    // 권한 검증이 없음 - 누구나 실종자 정보 수정 가능
    MissingPerson missingPerson = missingPersonRepository.findById(id)
            .orElseThrow(...);
```

**문제점**: 실종자 정보 수정 시 등록자 확인 없이 인증된 사용자라면 누구나 수정 가능

**영향도**: 무단 데이터 수정, 데이터 무결성 침해

**수정안**:
```java
@Override
@Transactional
public RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request) {
    User currentUser = getCurrentUser();

    // 실종 케이스 조회 및 권한 검증
    MissingCase missingCase = missingCaseRepository.findByMissingPersonId(id)
        .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_CASE_NOT_FOUND));

    missingCase.getReportedBy().validateUserAccess(currentUser);

    MissingPerson missingPerson = missingCase.getMissingPerson();
    // ... 나머지 로직
}
```

---

### 9. 초대 관련 엔드포인트 인증 부재

**파일**: `src/main/java/baro/baro/config/SecurityConfig.java:59-62`

```java
// Member Controller - 초대 관련 엔드포인트 (인증 불필요)
.requestMatchers(HttpMethod.POST, "/members/invitations/acceptance").permitAll()
.requestMatchers(HttpMethod.DELETE, "/members/invitations/rejection").permitAll()
.requestMatchers(HttpMethod.POST, "/members/invitations").permitAll()
```

**문제점**:
- 초대 수락/거부/생성이 인증 없이 가능
- 악의적 사용자가 임의로 초대 조작 가능

**영향도**: 권한 탈취, 데이터 조작

**수정안**: 해당 엔드포인트들에 인증 적용

---

### 10. Placeholder 이미지 반환 시 사용자 알림 부재

**파일**: `src/main/java/baro/baro/domain/ai/service/GoogleGenAiService.java:341-344, 356-357`

```java
if (geminiApiKey == null || geminiApiKey.isEmpty()) {
    log.warn("Gemini API Key가 설정되지 않음 - Placeholder 이미지 반환");
    return generatePlaceholderImage();  // 1x1 픽셀 JPEG
}

if (quotaCheckEnabled) {
    if (!rateLimiter.tryAcquire(quotaRpm, quotaRpd)) {
        return generatePlaceholderImage();  // 사용자는 성공으로 착각
    }
}
```

**문제점**:
- API 키 누락이나 Rate limit 초과 시 1x1 픽셀 이미지 반환
- 사용자는 실제 이미지가 생성된 것으로 착각
- 프로덕션에서 심각한 UX 문제 및 신뢰도 하락

**영향도**: 사용자 경험 저하, 비즈니스 신뢰도 하락

**수정안**:
```java
if (geminiApiKey == null || geminiApiKey.isEmpty()) {
    log.error("Gemini API Key가 설정되지 않음");
    throw new AiException(AiErrorCode.API_KEY_NOT_CONFIGURED);
}

if (quotaCheckEnabled && !rateLimiter.tryAcquire(quotaRpm, quotaRpd)) {
    log.warn("Rate limit 초과");
    throw new AiException(AiErrorCode.RATE_LIMIT_EXCEEDED);
}
```

---

## 🟡 심각도 3: 설계/아키텍처 문제

### 11. CORS 설정 - 프로덕션 위험

**파일**: `src/main/java/baro/baro/config/SecurityConfig.java:122`

```java
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
```

**문제점**:
- 모든 오리진 허용은 CSRF 및 데이터 유출 위험
- `allowCredentials(true)`와 함께 사용 시 특히 위험

**수정안**:
```java
@Value("${cors.allowed-origins}")
private List<String> allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    // ...
}
```

**application.yml 예시**:
```yaml
cors:
  allowed-origins:
    - https://baro-app.com
    - https://admin.baro-app.com
```

---

### 12. Actuator 엔드포인트 전체 공개

**파일**: `src/main/java/baro/baro/config/SecurityConfig.java:84`

```java
.requestMatchers("/actuator/**").permitAll()
```

**문제점**:
- `/actuator/env` - 환경변수 노출 (API 키, DB 비밀번호 등)
- `/actuator/heapdump` - 힙 덤프로 민감 정보 추출 가능
- `/actuator/configprops` - 설정 정보 노출

**영향도**: 민감 정보 유출, 시스템 취약점 노출

**수정안**:
```java
// 공개 엔드포인트만 허용
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
// 나머지는 관리자만
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

**application.yml에서 노출 엔드포인트 제한**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

### 13. Firebase 초기화 실패 시 애플리케이션 계속 실행

**파일**: `src/main/java/baro/baro/config/FirebaseConfig.java:51-53`

```java
} catch (IOException e) {
    log.error("Firebase 초기화 중 오류 발생: {}", e.getMessage(), e);
    // 예외를 삼키고 애플리케이션 계속 실행
}
```

**문제점**:
- Firebase 초기화 실패 시 푸시 알림이 전혀 작동하지 않음
- 실종자 발견 알림이 전송되지 않아 심각한 비즈니스 영향
- 사용자는 알림이 발송된 것으로 착각

**영향도**: 핵심 기능 장애, 비즈니스 영향

**수정안**:
```java
@PostConstruct
public void initialize() {
    try {
        if (FirebaseApp.getApps().isEmpty()) {
            // ... 초기화 로직
        }
    } catch (IOException e) {
        log.error("Firebase 초기화 실패 - 애플리케이션 시작 불가", e);
        throw new IllegalStateException("Firebase 초기화는 필수입니다", e);
    }
}
```

---

### 14. 실종자 등록 제한 우회 가능 (Race Condition)

**파일**: `src/main/java/baro/baro/domain/missingperson/service/MissingPersonServiceImpl.java:60-63`

```java
long registeredCount = missingCaseRepository.countByReportedById(currentUser.getId());
if (registeredCount >= 4) {
    throw new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_LIMIT_EXCEEDED);
}
```

**문제점**:
- 동시 요청 시 race condition으로 제한(4개) 우회 가능
- 두 요청이 동시에 count 조회 시 둘 다 통과할 수 있음

**수정안 1 - 비관적 락**:
```java
// Repository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT COUNT(mc) FROM MissingCase mc WHERE mc.reportedBy.id = :userId")
long countByReportedByIdWithLock(@Param("userId") Long userId);
```

**수정안 2 - DB 제약조건**:
```sql
-- 트리거 또는 체크 제약조건 사용
CREATE OR REPLACE FUNCTION check_missing_person_limit()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM missing_cases WHERE reported_by = NEW.reported_by) >= 4 THEN
        RAISE EXCEPTION 'User has reached maximum missing person limit';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

### 15. User 엔티티에서 직접 예외 던지기

**파일**: `src/main/java/baro/baro/domain/user/entity/User.java:181-186`

```java
public void validateUserAccess(User currentUser) {
    if (currentUser.getRole() == UserRole.ADMIN)
        return;
    if (!this.id.equals(currentUser.getId()))
        throw new UserException(UserErrorCode.USER_ACCESS_DENIED);
}
```

**문제점**:
- 엔티티가 특정 예외 클래스에 의존 (도메인 계층 위반)
- 테스트하기 어려움
- 단일 책임 원칙(SRP) 위반

**수정안**:
```java
// Entity에서는 boolean 반환
public boolean hasAccess(User currentUser) {
    return currentUser.getRole() == UserRole.ADMIN
        || this.id.equals(currentUser.getId());
}

// Service에서 예외 처리
if (!owner.hasAccess(currentUser)) {
    throw new UserException(UserErrorCode.USER_ACCESS_DENIED);
}
```

---

## 🔵 심각도 4: 개선 권장 사항

### 16. 매직 넘버/문자열 사용

**파일들**:
- `User.java:83`: `"http://example.com/default-profile.png"`
- `User.java:84`: `"#FEFFED"`
- `User.java:88`: `"수색 초보자"`

**문제점**: 하드코딩된 상수들이 엔티티에 직접 포함되어 유지보수 어려움

**수정안**:
```java
// 상수 클래스 생성
public class UserDefaults {
    public static final String DEFAULT_PROFILE_URL = "http://example.com/default-profile.png";
    public static final String DEFAULT_BACKGROUND_COLOR = "#FEFFED";
    public static final String DEFAULT_TITLE = "수색 초보자";
    public static final int DEFAULT_LEVEL = 1;
    public static final int DEFAULT_EXP = 0;
    public static final int DEFAULT_CARD = 1;
}

// 또는 application.yml에서 관리
user:
  defaults:
    profile-url: http://example.com/default-profile.png
    background-color: "#FEFFED"
    title: "수색 초보자"
```

---

### 17. JWT 토큰 파싱 예외 처리 불완전

**파일**: `src/main/java/baro/baro/domain/auth/service/JwtTokenProvider.java:146-157`

```java
public Long getDeviceIdFromToken(String token) {
    try {
        Claims claims = ...
        return claims.get("deviceId", Long.class);
    } catch (ExpiredJwtException e) {
        return e.getClaims().get("deviceId", Long.class);
    }
    // 다른 예외 시 예외가 전파되거나 null 반환
}
```

**문제점**:
- `MalformedJwtException`, `SignatureException` 등 다른 예외 시 처리 부재
- null 반환으로 `NullPointerException` 유발 가능

**수정안**:
```java
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
    } catch (JwtException e) {
        log.warn("JWT에서 deviceId 추출 실패: {}", e.getMessage());
        return null;
    }
}
```

---

### 18. 시간 타입 불일치

**파일들**:
- `User.java`: `ZonedDateTime` (createdAt, updatedAt)
- `Device.java`: `LocalDateTime` (registeredAt)
- `GpsTrack.java`: `LocalDateTime` (recordedAt)

**문제점**:
- 시간대 처리가 일관되지 않음
- 글로벌 서비스 확장 시 문제 발생 가능

**수정안**: 모든 시간 필드를 `ZonedDateTime` 또는 `Instant`로 통일
```java
@Column(name = "registered_at", nullable = false)
private ZonedDateTime registeredAt;
```

---

## 요약 테이블

| 우선순위 | 문제 | 파일:라인 | 영향도 | 카테고리 |
|---------|------|-----------|--------|----------|
| 🔴 1 | 알림 API 인증 미적용 | SecurityConfig.java:89-96 | 개인정보 유출 | 보안 |
| 🔴 1 | @Async + @Transactional 충돌 | DeviceServiceImpl.java:228-230 | 데이터 불일치 | 동시성 |
| 🔴 1 | 중복 import | MissingPersonServiceImpl.java:11-17 | 컴파일 경고 | 코드 품질 |
| 🔴 1 | Rate Limiter 동시성 | RateLimiter.java:24,104-111 | Race condition | 동시성 |
| 🟠 2 | 나이 계산 오류 | MissingPerson.java:95-107 | 비즈니스 로직 오류 | 로직 |
| 🟠 2 | 토큰 만료 시간 계산 | AuthServiceImpl.java:86-87 | 보안 취약 | 보안 |
| 🟠 2 | CompletableFuture 스레드풀 | GoogleGenAiService.java:106-114 | 성능 저하 | 성능 |
| 🟠 2 | 실종자 수정 권한 검증 누락 | MissingPersonServiceImpl.java:104 | 무단 수정 | 보안 |
| 🟠 2 | 초대 API 인증 부재 | SecurityConfig.java:59-62 | 권한 탈취 | 보안 |
| 🟠 2 | Placeholder 이미지 무음 반환 | GoogleGenAiService.java:341-357 | UX 문제 | UX |
| 🟡 3 | CORS 전체 허용 | SecurityConfig.java:122 | 보안 위험 | 보안 |
| 🟡 3 | Actuator 전체 공개 | SecurityConfig.java:84 | 정보 노출 | 보안 |
| 🟡 3 | Firebase 실패 무시 | FirebaseConfig.java:51-53 | 알림 실패 | 안정성 |
| 🟡 3 | 실종자 등록 제한 우회 | MissingPersonServiceImpl.java:60-63 | 제한 우회 | 동시성 |
| 🟡 3 | 엔티티에서 예외 던지기 | User.java:181-186 | 아키텍처 위반 | 설계 |
| 🔵 4 | 매직 넘버 사용 | User.java:83-88 | 유지보수 어려움 | 코드 품질 |
| 🔵 4 | JWT 예외 처리 불완전 | JwtTokenProvider.java:146-157 | NPE 가능성 | 안정성 |
| 🔵 4 | 시간 타입 불일치 | 여러 엔티티 | 일관성 부족 | 설계 |

---

## 권장 수정 순서

### Phase 1: 즉시 수정 (1-2일)
1. 인증 관련 SecurityConfig 수정 (문제 1, 9)
2. Actuator 엔드포인트 보안 (문제 12)
3. 중복 import 정리 (문제 3)

### Phase 2: 단기 수정 (1주일)
4. @Async + @Transactional 분리 (문제 2)
5. 나이 계산 로직 수정 (문제 5)
6. 토큰 만료 시간 계산 수정 (문제 6)
7. 권한 검증 추가 (문제 8)

### Phase 3: 중기 개선 (2-3주)
8. Rate Limiter 동시성 개선 (문제 4)
9. CompletableFuture 스레드풀 설정 (문제 7)
10. Placeholder 이미지 예외 처리 (문제 10)
11. Firebase 실패 처리 (문제 13)

### Phase 4: 장기 리팩토링 (1개월)
12. CORS 환경별 설정 분리 (문제 11)
13. 실종자 등록 제한 락 적용 (문제 14)
14. 엔티티 설계 개선 (문제 15, 16, 18)
15. JWT 예외 처리 보완 (문제 17)

---

## 참고 사항

- 본 리뷰는 코드 정적 분석을 기반으로 작성되었습니다.
- 실제 런타임 동작은 테스트를 통해 추가 검증이 필요합니다.
- 보안 관련 이슈는 보안 전문가의 추가 검토를 권장합니다.
