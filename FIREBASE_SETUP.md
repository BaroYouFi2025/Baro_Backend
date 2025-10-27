# 🔥 Firebase 설정 가이드

## 📋 개요

구성원 초대 시 푸시 알림 기능을 위한 Firebase Cloud Messaging (FCM) 설정 가이드입니다.

## 🔧 서버 환경변수 설정

### 1. 환경변수 추가

#### **.env 파일에 추가:**

```bash
# Firebase Configuration
FIREBASE_PROJECT_ID=baroyofi
FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json
```

#### **서버 환경변수로 설정:**

```bash
# Linux/Mac
export FIREBASE_PROJECT_ID=baroyofi
export FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json

# Windows
set FIREBASE_PROJECT_ID=baroyofi
set FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json
```

### 2. Docker 환경에서 설정

#### **docker-compose.yml:**

```yaml
services:
  baro-backend:
    environment:
      - FIREBASE_PROJECT_ID=baroyofi
      - FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json
```

#### **Dockerfile:**

```dockerfile
ENV FIREBASE_PROJECT_ID=baroyofi
ENV FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json
```

### 3. Kubernetes 환경에서 설정

#### **ConfigMap:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: baro-config
data:
  FIREBASE_PROJECT_ID: "baroyofi"
  FIREBASE_CREDENTIALS_PATH: "classpath:firebase-adminsdk.json"
```

#### **Secret (키 파일용):**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: firebase-secret
type: Opaque
data:
  firebase-adminsdk.json: <base64-encoded-key-file>
```

## 🔑 Firebase 서비스 계정 키 설정

### 1. Firebase Console에서 키 다운로드

1. [Firebase Console](https://console.firebase.google.com/) 접속
2. `baroyofi` 프로젝트 선택
3. **프로젝트 설정** → **서비스 계정** 탭
4. **새 비공개 키 생성** 클릭
5. JSON 파일 다운로드

### 2. 키 파일 배치

```bash
# 개발 환경
src/main/resources/firebase-adminsdk.json

# 운영 환경 (권장)
/opt/baro/config/firebase-adminsdk.json
```

### 3. 환경변수 경로 설정

```bash
# 개발 환경
FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json

# 운영 환경
FIREBASE_CREDENTIALS_PATH=file:/opt/baro/config/firebase-adminsdk.json
```

## 🚀 애플리케이션 시작 확인

### 1. 로그 확인

```bash
# Firebase 초기화 성공 로그
Firebase Admin SDK 초기화 완료 - 프로젝트 ID: baroyofi

# Firebase 초기화 실패 로그
Firebase가 초기화되지 않았습니다. Firebase 설정을 확인해주세요.
```

### 2. 헬스체크

```bash
curl http://localhost:8080/actuator/health
```

## ⚠️ 보안 주의사항

1. **키 파일은 절대 Git에 커밋하지 마세요**
2. **운영 환경에서는 별도 키 사용 권장**
3. **키 파일 권한을 600으로 설정**
4. **정기적으로 키 로테이션**

## 🧪 테스트 방법

### 1. FCM 토큰 발급

```bash
# 웹 브라우저에서 fcm-test.html 실행
open fcm-test.html
```

### 2. API 테스트

```bash
# Postman 컬렉션 사용
fcm-test-postman.json
```

### 3. 터미널 테스트

```bash
# FCM 메시지 발송 테스트
./test-fcm.sh
```

## 📞 문제 해결

### 1. Firebase 초기화 실패

- 키 파일 경로 확인
- 키 파일 내용 검증
- 환경변수 설정 확인

### 2. 푸시 알림 발송 실패

- FCM 토큰 유효성 확인
- 네트워크 연결 확인
- Firebase 프로젝트 설정 확인

## 📚 관련 문서

- [Firebase Admin SDK 문서](https://firebase.google.com/docs/admin/setup)
- [FCM 서버 프로토콜](https://firebase.google.com/docs/cloud-messaging/server)
