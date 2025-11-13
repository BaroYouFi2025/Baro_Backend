# 🧪 Postman 푸시 알림 테스트 가이드

## 📋 개요
구성원 초대 시 푸시 알림 기능을 Postman으로 테스트하는 방법입니다.

## 🚀 테스트 준비

### 1. Postman 컬렉션 Import
1. **Postman 실행**
2. **Import** 버튼 클릭
3. **`Baro_Push_Notification_Test.postman_collection.json`** 파일 선택
4. **Import** 클릭

### 2. 환경 변수 설정
Postman에서 **Environment** 생성 후 다음 변수들 설정:

| 변수명 | 값 | 설명 |
|--------|-----|------|
| `base_url` | `http://localhost:8080` | Baro API 서버 URL |
| `jwt_token` | `(로그인 후 설정)` | JWT 토큰 |
| `invitee_jwt_token` | `(로그인 후 설정)` | 초대받은 사용자 JWT 토큰 |
| `fcm_token` | `(웹에서 발급)` | FCM 토큰 |
| `new_fcm_token` | `(새로운 토큰)` | 새로운 FCM 토큰 |
| `firebase_server_key` | `(Firebase Console에서)` | Firebase Server Key |

## 🔥 FCM 토큰 발급 방법

### 1. 웹 브라우저에서 발급
```html
<!-- fcm-test.html 파일을 브라우저에서 열기 -->
<script>
// Firebase 설정
const firebaseConfig = {
  apiKey: "your-api-key",
  authDomain: "baroyofi.firebaseapp.com",
  projectId: "baroyofi",
  // ... 기타 설정
};

// FCM 토큰 발급
messaging.getToken().then(token => {
  console.log('FCM Token:', token);
  // 이 토큰을 Postman의 fcm_token 변수에 입력
});
</script>
```

### 2. Firebase Console에서 Server Key 발급
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. `baroyofi` 프로젝트 선택
3. **프로젝트 설정** → **클라우드 메시징** 탭
4. **서버 키** 복사
5. Postman의 `firebase_server_key` 변수에 입력

## 📱 테스트 시나리오

### **시나리오 1: 기본 푸시 알림 테스트**
1. **1. 사용자 로그인** 실행
2. 응답에서 `jwt_token` 복사하여 환경변수에 설정
3. **2. 기기 등록** 실행 (FCM 토큰 포함)
4. **4. 구성원 초대 요청** 실행
5. **결과 확인**: 초대받은 사용자 기기에 푸시 알림 발송

### **시나리오 2: 초대 응답 테스트**
1. 초대받은 사용자로 **1. 사용자 로그인** 실행
2. `invitee_jwt_token` 설정
3. **5. 초대 수락** 또는 **6. 초대 거절** 실행
4. **결과 확인**: 초대한 사용자 기기에 응답 알림 발송

### **시나리오 3: FCM 직접 테스트**
1. **7. FCM 직접 테스트** 실행
2. Firebase Server Key 설정
3. FCM 토큰 설정
4. **결과 확인**: 직접 푸시 알림 발송

## 🔍 테스트 결과 확인

### 1. API 응답 확인
- **200 OK**: 성공
- **400 Bad Request**: 요청 데이터 오류
- **401 Unauthorized**: 인증 실패
- **404 Not Found**: 리소스 없음

### 2. 푸시 알림 확인
- **웹 브라우저**: 알림 팝업 표시
- **모바일 앱**: 푸시 알림 수신
- **로그 확인**: Firebase 초기화 및 발송 로그

### 3. 데이터베이스 확인
```sql
-- 알림 이력 확인
SELECT * FROM youfi.notifications ORDER BY created_at DESC;

-- 기기 FCM 토큰 확인
SELECT device_uuid, fcm_token FROM youfi.devices WHERE fcm_token IS NOT NULL;
```

## ⚠️ 주의사항

1. **서버 실행 확인**: `http://localhost:8080` 접근 가능한지 확인
2. **Firebase 설정**: 실제 서비스 계정 키 파일 배치 확인
3. **데이터베이스**: PostgreSQL + PostGIS 실행 확인
4. **네트워크**: FCM 서버 접근 가능한지 확인

## 🐛 문제 해결

### 1. Firebase 초기화 실패
```
Firebase가 초기화되지 않았습니다. Firebase 설정을 확인해주세요.
```
**해결**: 서비스 계정 키 파일 경로 및 내용 확인

### 2. FCM 토큰 발급 실패
**해결**: 
- 브라우저 알림 권한 허용
- Firebase 프로젝트 설정 확인
- VAPID 키 설정 확인

### 3. 푸시 알림 발송 실패
**해결**:
- FCM 토큰 유효성 확인
- Firebase Server Key 확인
- 네트워크 연결 확인

## 📞 지원

문제가 발생하면 다음 정보와 함께 문의하세요:
- 에러 메시지
- 요청/응답 로그
- 환경 설정 정보
