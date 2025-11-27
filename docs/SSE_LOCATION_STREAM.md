# SSE 기반 실시간 위치 스트리밍

## 개요

구성원의 GPS 위치 변경을 실시간으로 수신하는 Server-Sent Events (SSE) 기반 스트리밍 API입니다.

## API 엔드포인트

### GET /members/locations/stream

기존 `GET /members/locations` (스냅샷)과 별도로 실시간 스트리밍을 제공합니다.

**Request**
```http
GET /members/locations/stream HTTP/1.1
Authorization: Bearer {access_token}
Accept: text/event-stream
```

**Response**
```
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
```

## 이벤트 타입

### MemberLocationEvent

```json
{
  "type": "INITIAL | UPDATE | HEARTBEAT",
  "timestamp": "2024-01-15T10:30:00",
  "payload": [
    {
      "userId": 1,
      "name": "김실종",
      "relationship": "가족",
      "batteryLevel": 45,
      "distance": 0.1,
      "location": {
        "latitude": 35.1763,
        "longitude": 128.9664
      }
    }
  ]
}
```

| 타입 | 설명 | payload |
|------|------|---------|
| `INITIAL` | 연결 직후 전체 구성원 위치 | 전체 목록 |
| `UPDATE` | 구성원 위치 변경 시 | 전체 목록 |
| `HEARTBEAT` | 연결 유지용 (15초 간격) | null |

## 아키텍처

### 컴포넌트 구조

```
MemberController (SSE 엔드포인트)
       ↓
MemberLocationEmitterRegistry (연결 관리)
       ↑
MemberLocationEventListener (@Async)
       ↑
MemberLocationChangedEvent
       ↑
DeviceServiceImpl#updateGps (이벤트 발행)
```

### 이벤트 흐름

1. **연결 수립**
   - 클라이언트가 `/members/locations/stream` 연결
   - `SseEmitter` 생성 (타임아웃: 30분)
   - `EmitterRegistry`에 userId → emitter 매핑 저장
   - INITIAL 이벤트로 현재 구성원 위치 전송
   - Heartbeat 스케줄러 시작 (15초 간격)

2. **위치 업데이트**
   - 구성원이 `POST /devices/{deviceId}/gps` 호출
   - `DeviceServiceImpl`이 GPS 저장 후 `MemberLocationChangedEvent` 발행
   - `MemberLocationEventListener`가 비동기로 이벤트 수신
   - `RelationshipRepository.findUserIdsByMemberId()`로 관련 사용자 조회
   - 각 사용자의 emitter에 UPDATE 이벤트 브로드캐스트

3. **연결 종료**
   - 타임아웃, 클라이언트 종료, 오류 발생 시
   - `onCompletion`, `onTimeout`, `onError` 콜백에서 정리
   - Heartbeat 스케줄러 종료
   - `EmitterRegistry`에서 emitter 제거

## 주요 클래스

### MemberLocationEmitterRegistry

```java
@Component
public class MemberLocationEmitterRegistry {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void addEmitter(Long userId, SseEmitter emitter);
    public void removeEmitter(Long userId, SseEmitter emitter);
    public void broadcast(Long userId, MemberLocationEvent event);
    public boolean hasConnection(Long userId);
}
```

- `ConcurrentHashMap<Long, List<SseEmitter>>`로 사용자별 다중 연결 지원
- 내부 리스트는 `CopyOnWriteArrayList`로 동시성 안전
- 전송 실패 시 해당 emitter 자동 제거

### MemberLocationEventListener

```java
@Component
public class MemberLocationEventListener {

    @Async
    @EventListener
    public void handleLocationChanged(MemberLocationChangedEvent event) {
        // 1. 관련 사용자 조회
        List<Long> relatedUserIds = relationshipRepository.findUserIdsByMemberId(userId);

        // 2. 각 사용자에게 브로드캐스트
        for (Long userId : relatedUserIds) {
            if (emitterRegistry.hasConnection(userId)) {
                List<MemberLocationResponse> locations = memberService.getMemberLocationsForUser(userId);
                emitterRegistry.broadcast(userId, MemberLocationEvent.update(locations));
            }
        }
    }
}
```

- `@Async`로 비동기 처리 (메인 요청 블로킹 방지)
- 연결이 없는 사용자는 스킵하여 불필요한 DB 조회 방지

## 설정값

| 설정 | 값 | 설명 |
|------|-----|------|
| SSE_TIMEOUT | 30분 | Emitter 타임아웃 |
| HEARTBEAT_INTERVAL | 15초 | Heartbeat 전송 간격 |

## 클라이언트 구현 가이드

### JavaScript (EventSource)

```javascript
const eventSource = new EventSource('/members/locations/stream', {
  headers: { 'Authorization': 'Bearer ' + accessToken }
});

// 이벤트 수신
eventSource.addEventListener('location', (event) => {
  const data = JSON.parse(event.data);

  switch (data.type) {
    case 'INITIAL':
      console.log('초기 데이터:', data.payload);
      initializeMap(data.payload);
      break;
    case 'UPDATE':
      console.log('위치 업데이트:', data.payload);
      updateMarkers(data.payload);
      break;
    case 'HEARTBEAT':
      console.log('연결 유지');
      break;
  }
});

// 오류 처리
eventSource.onerror = (error) => {
  console.error('SSE 연결 오류:', error);
  // 재연결 로직
};

// 연결 종료
function disconnect() {
  eventSource.close();
}
```

### React Native / Mobile

```javascript
import EventSource from 'react-native-sse';

const eventSource = new EventSource(
  `${API_BASE_URL}/members/locations/stream`,
  {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);

eventSource.addEventListener('location', (event) => {
  const data = JSON.parse(event.data);
  // 처리 로직
});
```

### 재연결 전략

```javascript
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 3000;

function connect() {
  const eventSource = new EventSource('/members/locations/stream', {
    headers: { 'Authorization': 'Bearer ' + accessToken }
  });

  eventSource.onopen = () => {
    reconnectAttempts = 0;
  };

  eventSource.onerror = () => {
    eventSource.close();

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      reconnectAttempts++;
      setTimeout(connect, RECONNECT_DELAY * reconnectAttempts);
    }
  };
}
```

## 프록시 설정

### Nginx

```nginx
location /members/locations/stream {
    proxy_pass http://backend;
    proxy_http_version 1.1;
    proxy_set_header Connection '';
    proxy_buffering off;
    proxy_cache off;

    # SSE 타임아웃 설정
    proxy_read_timeout 3600s;
    proxy_send_timeout 3600s;
}
```

### 응답 헤더

SSE 응답에는 다음 헤더가 자동 설정됩니다:
- `Content-Type: text/event-stream`
- `Cache-Control: no-cache`
- `X-Accel-Buffering: no` (Nginx 버퍼링 비활성화)

## 성능 고려사항

### 최적화 전략

1. **DTO 재사용**: 동일 이벤트를 여러 emitter에 전송 시 JSON 직렬화 1회만 수행
2. **연결 확인**: `hasConnection()` 체크로 불필요한 DB 조회 방지
3. **비동기 처리**: `@Async`로 이벤트 리스너 분리
4. **선택적 브로드캐스트**: 관련 사용자만 조회하여 전송

### 리소스 관리

- Heartbeat 스케줄러: `ScheduledExecutorService` 사용
- emitter 완료/오류 시 스케줄러 자동 종료
- 실패한 emitter 즉시 제거

## 확장 고려사항

### WebSocket 마이그레이션

향후 양방향 통신이 필요할 경우를 대비한 인터페이스 추상화:

```java
public interface LocationBroadcaster {
    void addConnection(Long userId, Object connection);
    void removeConnection(Long userId, Object connection);
    void broadcast(Long userId, Object payload);
}
```

### 수평 확장

다중 서버 환경에서는 Redis Pub/Sub 등을 통한 이벤트 동기화 필요:

```
Server A (위치 업데이트)
    → Redis Pub/Sub
        → Server B (SSE 연결 보유)
            → 클라이언트 브로드캐스트
```

## 모니터링

### 연결 상태 확인

```java
@GetMapping("/admin/sse/stats")
public Map<String, Object> getSseStats() {
    return Map.of(
        "totalConnections", emitterRegistry.getTotalConnectionCount()
    );
}
```

### 로그 레벨

- `DEBUG`: 연결/해제, 이벤트 전송 상세
- `WARN`: 전송 실패, 예외 상황
- `ERROR`: 치명적 오류

## 테스트

### curl 테스트

```bash
curl -N -H "Authorization: Bearer {token}" \
     -H "Accept: text/event-stream" \
     http://localhost:8080/members/locations/stream
```

### 단위 테스트

```java
@Test
void shouldBroadcastLocationUpdate() {
    // given
    Long userId = 1L;
    SseEmitter emitter = mock(SseEmitter.class);
    registry.addEmitter(userId, emitter);

    // when
    registry.broadcast(userId, MemberLocationEvent.heartbeat());

    // then
    verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
}
```
