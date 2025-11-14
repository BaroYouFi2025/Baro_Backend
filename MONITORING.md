# Baro 모니터링 시스템

Actuator + Prometheus + Grafana 기반 종합 모니터링 시스템

## 📊 시스템 구성

```
┌─────────────┐
│  Spring Boot│
│  Actuator   │ ──── /actuator/prometheus
└─────────────┘          │
                         ▼
                  ┌─────────────┐
                  │ Prometheus  │ ──── 메트릭 수집 및 저장
                  └─────────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │  Grafana    │ ──── 시각화 및 알림
                  └─────────────┘
```

## 🚀 빠른 시작

### 1. 빌드

```bash
# JAR 파일 생성
./gradlew clean bootJar

# Docker 이미지 빌드
docker-compose build
```

### 2. 실행

```bash
# 전체 스택 시작 (PostgreSQL + App + Prometheus + Grafana)
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 3. 접속

| 서비스 | URL | 기본 계정 |
|--------|-----|-----------|
| **애플리케이션** | http://localhost:8080 | - |
| **Actuator** | http://localhost:8080/actuator | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin |

## 📈 모니터링 메트릭

### 기본 메트릭 (자동 수집)

**JVM**:
- `jvm_memory_used_bytes` - JVM 메모리 사용량
- `jvm_memory_max_bytes` - JVM 최대 메모리
- `jvm_gc_pause_seconds_count` - GC 횟수
- `jvm_gc_pause_seconds_sum` - GC 소요 시간
- `jvm_threads_live` - 활성 쓰레드 수

**HTTP 요청**:
- `http_server_requests_seconds_count` - 총 요청 수
- `http_server_requests_seconds_sum` - 총 응답 시간
- URI, Method, Status 코드별로 태그 구분

**데이터베이스**:
- `hikaricp_connections_active` - 활성 커넥션 수
- `hikaricp_connections_idle` - 대기 커넥션 수
- `hikaricp_connections_max` - 최대 커넥션 수

### 커스텀 비즈니스 메트릭

#### 1. 실종자 관련
```
missing_person_reports_total{type="new"}       # 실종자 신고 수
missing_person_found_total{status="found"}     # 실종자 발견 수
```

#### 2. GPS 위치 추적
```
gps_location_updates_total                     # GPS 위치 업데이트 수
gps_update_duration_seconds                    # GPS 업데이트 소요 시간
```

#### 3. AI 이미지 생성
```
ai_image_generation_success_total{asset_type="AGE_PROGRESSION"}    # 성공 수
ai_image_generation_failure_total{asset_type="...",error_type="..."} # 실패 수
ai_generation_duration_seconds{asset_type="..."}                   # 소요 시간
```

#### 4. FCM 푸시 알림
```
fcm_messages_success_total{notification_type="NEARBY_ALERT"}       # 전송 성공 수
fcm_messages_failure_total{notification_type="...",error_type="..."}# 전송 실패 수
fcm_send_duration_seconds                                           # 전송 소요 시간
```

**알림 타입**:
- `invitation` - 초대 요청
- `missing_person_found` - 실종자 발견
- `nearby_alert` - 주변 실종자 알림

**에러 타입**:
- `INVALID_TOKEN` - 잘못된 FCM 토큰
- `UNREGISTERED` - 등록되지 않은 디바이스
- `QUOTA_EXCEEDED` - 할당량 초과
- `FIREBASE_NOT_INITIALIZED` - Firebase 미초기화
- `UNEXPECTED_ERROR` - 예상치 못한 에러

#### 5. 사용자 활동
```
user_logins_total                              # 로그인 수
user_registrations_total                       # 회원가입 수
```

## 🎯 Grafana 대시보드

### 기본 대시보드 (자동 생성됨)

**Baro Spring Boot 모니터링** 대시보드에서 확인 가능:

1. **애플리케이션 상태** - UP/DOWN 상태
2. **JVM 메모리 사용량** - Heap 메모리 사용률 (%)
3. **HTTP 요청 속도** - 초당 요청 수 (req/s)
4. **HTTP 응답 시간** - 평균 응답 시간
5. **HTTP 상태 코드** - 2xx, 4xx, 5xx 분포
6. **데이터베이스 커넥션 풀** - Active/Idle/Max
7. **GC 활동** - GC 횟수 및 소요 시간

### 커스텀 대시보드 추가 방법

1. Grafana 접속: http://localhost:3000
2. 좌측 메뉴 → Dashboards → New Dashboard
3. Add visualization → Prometheus 선택
4. 메트릭 쿼리 입력 (예: `fcm_messages_success_total`)
5. Save dashboard

**예제 쿼리**:

```promql
# FCM 성공률 (%)
100 * (
  sum(rate(fcm_messages_success_total[5m]))
  /
  (sum(rate(fcm_messages_success_total[5m])) + sum(rate(fcm_messages_failure_total[5m])))
)

# 알림 타입별 전송 수 (1시간)
sum by (notification_type) (increase(fcm_messages_success_total[1h]))

# AI 생성 평균 소요 시간 (초)
rate(ai_generation_duration_seconds_sum[5m]) / rate(ai_generation_duration_seconds_count[5m])
```

## 🚨 알림 규칙 설정

### Grafana Alerting 설정

1. **Grafana → Alerting → Alert rules → New alert rule**

#### 예제 1: API 응답 시간 임계치 초과

```yaml
Alert Name: API 응답 시간 초과
Query:
  rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m]) > 5

Condition:
  WHEN avg() OF query(A) IS ABOVE 5 (seconds)

Actions:
  - Send notification to email/slack
```

#### 예제 2: FCM 실패율 임계치 초과

```yaml
Alert Name: FCM 실패율 높음
Query:
  100 * (
    sum(rate(fcm_messages_failure_total[5m]))
    /
    (sum(rate(fcm_messages_success_total[5m])) + sum(rate(fcm_messages_failure_total[5m])))
  ) > 10

Condition:
  WHEN avg() OF query(A) IS ABOVE 10 (percent)
```

#### 예제 3: JVM 메모리 사용량 임계치 초과

```yaml
Alert Name: JVM 메모리 부족
Query:
  100 * (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 85

Condition:
  WHEN avg() OF query(A) IS ABOVE 85 (percent)
```

## 🔧 커스텀 메트릭 추가 방법

### 1. MetricsService에 메서드 추가

```java
// src/main/java/baro/baro/domain/common/monitoring/MetricsService.java

public void recordCustomMetric(String value) {
    Counter.builder("custom_metric_total")
            .description("커스텀 메트릭 설명")
            .tag("category", value)
            .register(meterRegistry)
            .increment();
}
```

### 2. 서비스에서 메트릭 기록

```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final MetricsService metricsService;

    public void yourMethod() {
        // 비즈니스 로직

        // 메트릭 기록
        metricsService.recordCustomMetric("success");
    }
}
```

### 3. Prometheus에서 확인

```bash
# 메트릭 쿼리
curl http://localhost:9090/api/v1/query?query=custom_metric_total

# 또는 Prometheus UI
http://localhost:9090/graph
```

## 🔍 문제 해결

### Prometheus가 메트릭을 수집하지 못함

1. **애플리케이션 메트릭 엔드포인트 확인**:
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

2. **Prometheus 타겟 상태 확인**:
   - http://localhost:9090/targets
   - `spring-actuator` 타겟이 UP 상태여야 함

3. **Docker 네트워크 확인**:
   ```bash
   docker-compose ps
   docker network inspect baro_default
   ```

### Grafana 대시보드가 비어있음

1. **데이터소스 연결 확인**:
   - Grafana → Configuration → Data sources
   - Prometheus가 연결되어 있는지 확인

2. **시간 범위 조정**:
   - 대시보드 우측 상단 시간 선택기에서 "Last 5 minutes" 선택

3. **데이터 존재 여부 확인**:
   - Prometheus에서 먼저 데이터를 확인

### 메트릭이 기록되지 않음

1. **로그 확인**:
   ```bash
   docker-compose logs -f app | grep "Metric recorded"
   ```

2. **MetricsService 주입 확인**:
   - 서비스에 `@RequiredArgsConstructor` 또는 생성자 주입 확인

3. **Actuator 의존성 확인**:
   ```bash
   ./gradlew dependencies | grep actuator
   ```

## 📝 환경 변수

`.env` 파일에 추가 가능한 모니터링 관련 환경 변수:

```env
# Grafana 관리자 계정
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your-secure-password

# Prometheus 데이터 보존 기간
PROMETHEUS_RETENTION_DAYS=15d
```

## 🎓 참고 자료

- [Spring Boot Actuator 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus 쿼리 가이드](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Grafana 대시보드 가이드](https://grafana.com/docs/grafana/latest/dashboards/)
- [Micrometer 공식 문서](https://micrometer.io/docs)

## 📞 지원

문제가 발생하면 다음을 확인하세요:

1. 로그: `docker-compose logs -f`
2. 헬스체크: `curl http://localhost:8080/actuator/health`
3. Prometheus 타겟: http://localhost:9090/targets
4. Grafana 데이터소스: http://localhost:3000/datasources
