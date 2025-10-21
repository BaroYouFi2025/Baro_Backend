# Device API 명세서

## 개요
기기 등록 및 GPS 위치 추적을 위한 REST API

**Base URL**: `/devices`

**인증**: Bearer Token (JWT) 필요

---

## API 엔드포인트

### 1. 기기 등록
새로운 기기를 사용자 계정에 등록합니다.

#### Request
```
POST /devices/register
```

**Headers**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**
```json
{
  "deviceUuid": "550e8400-e29b-41d4-a716-446655440000",
  "osType": "iOS",
  "osVersion": "17.0"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| deviceUuid | UUID | O | 클라이언트가 생성한 기기 고유 식별자 |
| osType | String | X | 운영체제 타입 (iOS, Android 등) |
| osVersion | String | X | 운영체제 버전 |

#### Response
**Success (200 OK)**
```json
{
  "deviceId": 123,
  "deviceUuid": "550e8400-e29b-41d4-a716-446655440000",
  "batteryLevel": null,
  "osType": "iOS",
  "osVersion": "17.0",
  "isActive": true,
  "registeredAt": "2025-10-20T12:00:00"
}
```

**Error Responses**
- `400 Bad Request`: 이미 등록된 UUID
- `401 Unauthorized`: 인증 실패

---

### 2. GPS 위치 업데이트
기기의 GPS 위치 정보를 업데이트합니다.

#### Request
```
POST /devices/{deviceId}/gps
```

**Headers**
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| deviceId | Long | 기기 ID |

**Request Body**
```json
{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "batteryLevel": 85
}
```

| 필드 | 타입 | 필수 | 범위 | 설명 |
|------|------|------|------|------|
| latitude | Double | O | -90 ~ 90 | 위도 |
| longitude | Double | O | -180 ~ 180 | 경도 |
| batteryLevel | Integer | X | 0 ~ 100 | 배터리 잔량 (%) |

#### Response
**Success (200 OK)**
```json
{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "recordedAt": "2025-10-20T12:00:00",
  "message": "GPS 위치가 업데이트되었습니다."
}
```

**Error Responses**
- `400 Bad Request`: 잘못된 위도/경도 값
- `401 Unauthorized`: 인증 실패
- `404 Not Found`: 기기를 찾을 수 없음 또는 소유권 없음

---

## 데이터 모델

### DeviceResponse
| 필드 | 타입 | 설명 |
|------|------|------|
| deviceId | Long | 기기 ID |
| deviceUuid | UUID | 기기 고유 식별자 |
| batteryLevel | Integer | 배터리 잔량 (0-100) |
| osType | String | 운영체제 타입 (iOS, Android 등) |
| osVersion | String | 운영체제 버전 |
| isActive | Boolean | 활성화 상태 |
| registeredAt | LocalDateTime | 등록 시간 |

### GpsUpdateResponse
| 필드 | 타입 | 설명 |
|------|------|------|
| latitude | Double | 위도 |
| longitude | Double | 경도 |
| recordedAt | LocalDateTime | 기록 시간 |
| message | String | 성공 메시지 |

---

## 에러 코드

| HTTP 상태 코드 | 설명 |
|----------------|------|
| 200 OK | 요청 성공 |
| 400 Bad Request | 잘못된 요청 (유효성 검증 실패) |
| 401 Unauthorized | 인증 실패 (토큰 없음 또는 만료) |
| 404 Not Found | 리소스를 찾을 수 없음 |
| 500 Internal Server Error | 서버 내부 오류 |

---

## 사용 예시

### cURL 예시

**1. 기기 등록**
```bash
curl -X POST http://localhost:8080/devices/register \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "deviceUuid": "550e8400-e29b-41d4-a716-446655440000",
    "osType": "iOS",
    "osVersion": "17.0"
  }'
```

**2. GPS 위치 업데이트**
```bash
curl -X POST http://localhost:8080/devices/123/gps \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 37.5665,
    "longitude": 126.9780,
    "batteryLevel": 85
  }'
```

---

## 참고사항

1. **UUID 생성**: 클라이언트에서 UUID를 생성하여 전송해야 합니다.
   - iOS: `UUID().uuidString`
   - Android: `UUID.randomUUID().toString()`

2. **좌표계**: WGS84 (SRID: 4326) 좌표계를 사용합니다.

3. **인증**: 모든 API는 JWT Bearer 토큰을 통한 인증이 필요합니다.

4. **배터리 레벨**: GPS 위치 업데이트 시 선택적으로 배터리 레벨을 전송하면 기기 정보가 업데이트됩니다.

5. **데이터베이스 스키마**: `youfi.devices` 및 `youfi.gps_tracks` 테이블 사용
