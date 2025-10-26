# Baro - 실종자 찾기 및 위치 추적 시스템

> 실종자 신고, GPS 기반 위치 추적, AI 얼굴 인식을 활용한 통합 실종자 관리 플랫폼

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [프로젝트 구조](#프로젝트-구조)
- [개발 가이드](#개발-가이드)
- [커밋 컨벤션](#커밋-컨벤션)
- [브랜치 컨벤션](#브랜치-컨벤션)

---

## 🎯 프로젝트 개요

**Baro**는 실종자를 신속하게 찾기 위한 통합 관리 시스템입니다.
GPS 기반 위치 추적, AI 얼굴 인식, 실시간 알림 등의 기술을 활용하여
실종자 수색의 효율성을 극대화합니다.

### 핵심 가치

- 🚨 **신속한 대응**: 실시간 위치 추적으로 빠른 발견 가능
- 🤖 **AI 기반 인식**: 얼굴 인식 기술로 자동 매칭
- 📱 **다중 기기 지원**: 여러 기기를 통한 동시 위치 추적
- 🔐 **안전한 인증**: JWT 기반 보안 시스템

---

## ✨ 주요 기능

### 1. 사용자 인증 (Auth)

- 이메일 기반 회원가입 및 로그인
- JWT Access/Refresh Token 발급
- 비밀번호 암호화 저장
- 인증 메일 발송

### 2. 기기 관리 (Device)

- 모바일 기기 등록 및 관리
- 기기별 배터리 상태 모니터링
- FCM 토큰 관리 및 푸시 알림 지원
- 다중 기기 지원
- 기기 활성화/비활성화

### 3. GPS 위치 추적 (GPS Tracking)

- 실시간 위치 정보 수집
- PostGIS 기반 공간 데이터 저장
- WGS84 좌표계 지원
- 위치 히스토리 관리

### 4. 실종자 관리 (Missing Person)

- 실종자 신고 등록
- 상세 정보 관리 (나이, 특징, 사진 등)
- 상태 업데이트 (실종중/발견)

### 5. AI 얼굴 인식

- 얼굴 이미지 분석
- 실종자 매칭
- 유사도 기반 검색

### 6. 푸시 알림 시스템 (Push Notification)

- Firebase Cloud Messaging (FCM) 연동
- 구성원 초대 요청 알림
- 초대 수락/거절 알림
- 알림 이력 관리

---

## 🛠 기술 스택

### Backend

- **Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Build Tool**: Gradle

### Database

- **RDBMS**: PostgreSQL
- **Spatial Extension**: PostGIS (위치 데이터)
- **ORM**: Spring Data JPA
- **Spatial Library**: Hibernate Spatial + JTS

### Security

- **Authentication**: JWT (JSON Web Token)
- **Encryption**: BCrypt Password Encoder
- **Framework**: Spring Security

### Documentation

- **API Docs**: SpringDoc OpenAPI 3.x (Swagger UI)
- **Access**: `/swagger-ui.html`

### External Libraries

- **Lombok**: 보일러플레이트 코드 감소
- **Dotenv**: 환경 변수 관리
- **Jakarta Mail**: 이메일 발송
- **Firebase Admin SDK**: 푸시 알림 (FCM)

### Testing

- **Framework**: JUnit 5
- **Integration Testing**: Testcontainers
- **Security Testing**: Spring Security Test

---

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- PostgreSQL 14+ (PostGIS 확장 포함)
- Gradle 7.x+

### 1. 프로젝트 클론

```bash
git clone https://github.com/BaroYouFi2025/Baro.git
cd Baro
```

### 2. 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```properties
# JWT Configuration
JWT_SECRET_KEY=your-jwt-secret-key-here
JWT_ACCESS_VALIDITY_SECONDS=3600
JWT_REFRESH_VALIDITY_SECONDS=604800

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/baro_db
DATABASE_USERNAME=baro_user
DATABASE_PASSWORD=your-database-password

# Email Configuration
MAIL_IMAP_HOST=imap.gmail.com
MAIL_IMAP_USERNAME=your-email@gmail.com
MAIL_IMAP_PASSWORD=your-app-password

# Firebase Configuration
FIREBASE_PROJECT_ID=baroyofi
FIREBASE_CREDENTIALS_PATH=classpath:firebase-adminsdk.json

# File Upload Configuration
FILE_UPLOAD_DIR=uploads
FILE_UPLOAD_BASE_URL=http://localhost:8080
```

```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/baro_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your-secret-key-here-minimum-256-bits
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# Mail Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# AI Service Configuration (Optional)
AI_API_URL=http://ai-service-url
AI_API_KEY=your-ai-api-key
```

### 3. 데이터베이스 설정

#### PostgreSQL + PostGIS 설치

```bash
# PostgreSQL 설치 (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib

# PostGIS 확장 설치
sudo apt-get install postgis postgresql-14-postgis-3
```

#### 데이터베이스 및 스키마 생성

```sql
-- 데이터베이스 생성
CREATE DATABASE baro_db;

-- PostGIS 확장 활성화
\c baro_db
CREATE EXTENSION postgis;

-- 스키마 생성
CREATE SCHEMA youfi;

-- 테이블은 JPA가 자동 생성하거나 마이그레이션 스크립트 실행
```

### 4. 빌드 및 실행

#### Gradle을 통한 빌드

```bash
# 빌드 (테스트 제외)
./gradlew clean build -x test

# 테스트 포함 빌드
./gradlew clean build
```

#### 애플리케이션 실행

```bash
# Gradle을 통한 실행
./gradlew bootRun

# JAR 파일 직접 실행
java -jar build/libs/baro-app.jar
```

애플리케이션은 기본적으로 **http://localhost:8080** 에서 실행됩니다.

### 5. API 문서 확인

브라우저에서 다음 URL로 접속:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## 📚 API 문서

### 인증 관련 API

| Method | Endpoint        | 설명      | 인증 필요 |
| ------ | --------------- | --------- | --------- |
| POST   | `/auth/signup`  | 회원가입  | ❌        |
| POST   | `/auth/login`   | 로그인    | ❌        |
| POST   | `/auth/refresh` | 토큰 갱신 | ✅        |
| POST   | `/auth/logout`  | 로그아웃  | ✅        |

### 기기 관리 API

| Method | Endpoint                  | 설명              | 인증 필요 |
| ------ | ------------------------- | ----------------- | --------- |
| POST   | `/devices/register`       | 기기 등록         | ✅        |
| GET    | `/devices`                | 내 기기 목록 조회 | ✅        |
| POST   | `/devices/{deviceId}/gps` | GPS 위치 업데이트 | ✅        |
| POST   | `/devices/fcm-token`      | FCM 토큰 업데이트 | ✅        |

### 구성원 관리 API

| Method | Endpoint                      | 설명             | 인증 필요 |
| ------ | ----------------------------- | ---------------- | --------- |
| POST   | `/members/invitations`        | 구성원 초대 요청 | ✅        |
| POST   | `/members/invitations/accept` | 초대 수락        | ✅        |
| POST   | `/members/invitations/reject` | 초대 거절        | ✅        |
| GET    | `/members/locations`          | 구성원 위치 조회 | ✅        |

### 실종자 관리 API

| Method | Endpoint                | 설명             | 인증 필요 |
| ------ | ----------------------- | ---------------- | --------- |
| POST   | `/missing-persons`      | 실종자 신고      | ✅        |
| GET    | `/missing-persons`      | 실종자 목록 조회 | ✅        |
| GET    | `/missing-persons/{id}` | 실종자 상세 조회 | ✅        |
| PUT    | `/missing-persons/{id}` | 실종자 정보 수정 | ✅        |
| DELETE | `/missing-persons/{id}` | 실종자 신고 삭제 | ✅        |

상세 API 명세는 [DEVICE_API.md](./DEVICE_API.md) 참조

---

## 📁 프로젝트 구조

```
baro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── baro/baro/
│   │   │       ├── BaroApplication.java          # 메인 애플리케이션
│   │   │       ├── config/                       # 설정 클래스
│   │   │       │   ├── SecurityConfig.java       # Spring Security 설정
│   │   │       │   ├── SwaggerConfig.java        # Swagger 설정
│   │   │       │   └── JwtConfig.java            # JWT 설정
│   │   │       └── domain/                       # 도메인 패키지
│   │   │           ├── auth/                     # 인증/인가
│   │   │           │   ├── controller/           # 컨트롤러
│   │   │           │   ├── service/              # 비즈니스 로직
│   │   │           │   ├── repository/           # 데이터 접근
│   │   │           │   ├── entity/               # 엔티티
│   │   │           │   ├── dto/                  # DTO
│   │   │           │   └── exception/            # 예외 처리
│   │   │           ├── user/                     # 사용자 및 기기
│   │   │           │   ├── controller/
│   │   │           │   │   └── DeviceController.java
│   │   │           │   ├── service/
│   │   │           │   │   ├── DeviceService.java
│   │   │           │   │   └── DeviceServiceImpl.java
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserRepository.java
│   │   │           │   │   └── DeviceRepository.java
│   │   │           │   ├── entity/
│   │   │           │   │   ├── User.java
│   │   │           │   │   └── Device.java
│   │   │           │   └── dto/
│   │   │           │       ├── req/
│   │   │           │       │   └── DeviceRegisterRequest.java
│   │   │           │       └── res/
│   │   │           │           └── DeviceResponse.java
│   │   │           ├── member/                   # 회원 정보
│   │   │           │   ├── entity/
│   │   │           │   │   └── GpsTrack.java    # GPS 추적 데이터
│   │   │           │   ├── repository/
│   │   │           │   │   └── GpsTrackRepository.java
│   │   │           │   └── dto/
│   │   │           │       ├── request/
│   │   │           │       │   └── GpsUpdateRequest.java
│   │   │           │       └── response/
│   │   │           │           └── GpsUpdateResponse.java
│   │   │           ├── missingperson/            # 실종자 관리
│   │   │           │   ├── controller/
│   │   │           │   └── entity/
│   │   │           ├── ai/                       # AI 얼굴 인식
│   │   │           ├── image/                    # 이미지 처리
│   │   │           └── common/                   # 공통 유틸리티
│   │   └── resources/
│   │       ├── application.properties            # 애플리케이션 설정
│   │       └── static/                           # 정적 리소스
│   └── test/                                     # 테스트 코드
├── build.gradle                                  # Gradle 빌드 설정
├── .env                                          # 환경 변수 (git 제외)
├── README.md                                     # 프로젝트 문서
└── DEVICE_API.md                                 # Device API 상세 문서
```

---

## 💻 개발 가이드

### 로컬 개발 환경 설정

#### 1. IDE 설정

- **추천 IDE**: IntelliJ IDEA
- Lombok 플러그인 설치 필요
- Annotation Processing 활성화

#### 2. 코드 스타일

- Java 17 기능 활용
- Lombok 적극 활용 (@Getter, @Builder 등)
- RESTful API 설계 원칙 준수
- 계층형 아키텍처 (Controller → Service → Repository)

#### 3. 데이터베이스 마이그레이션

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update  # 개발: update, 운영: validate
spring.jpa.show-sql=true              # SQL 로그 출력
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests DeviceServiceTest

# 통합 테스트 실행 (Testcontainers 사용)
./gradlew test --tests *IntegrationTest
```

### 패키징

```bash
# JAR 파일 생성
./gradlew bootJar

# 생성된 파일 위치
# build/libs/baro-app.jar
```

---

## 📝 커밋 컨벤션

### 커밋 타입

| Type         | Description                                  |
| ------------ | -------------------------------------------- |
| **feat**     | 새로운 기능 추가                             |
| **fix**      | 버그 수정                                    |
| **refactor** | 코드 리팩토링 (기능 변경 없이 구조 개선)     |
| **test**     | 테스트 코드 작성                             |
| **chore**    | 기타 자잘한 작업 (빌드 설정, 패키지 관리 등) |
| **docs**     | 문서 추가 또는 수정                          |
| **delete**   | 불필요한 코드나 파일 삭제                    |
| **build**    | 빌드 관련 파일 및 설정 변경                  |

### 커밋 메시지 형식

```
타입(#이슈번호) :: 변경 사항 요약
```

- **제목**: 50자 이내
- **본문**: 선택사항, 72자 이내로 요약 설명 권장

### 커밋 예시

```bash
feat(#29) :: Device 기기 등록 API 구현
fix(#45) :: GPS 위치 업데이트 시 배터리 레벨 반영 안 되는 버그 수정
refactor(#72) :: DeviceService 인터페이스 분리
docs(#88) :: Device API 명세서 작성
test(#90) :: DeviceController 통합 테스트 추가
```

---

## 🌿 브랜치 컨벤션

### 브랜치 전략

| Prefix        | 사용 상황                | 예시                         |
| ------------- | ------------------------ | ---------------------------- |
| **feat/**     | 새로운 기능 개발         | `feat/29-device-api`         |
| **fix/**      | 버그 수정                | `fix/45-gps-battery-bug`     |
| **hotfix/**   | 긴급 버그/패치           | `hotfix/urgent-deploy-error` |
| **refactor/** | 리팩토링                 | `refactor/72-user-service`   |
| **docs/**     | 문서 작업                | `docs/api-documentation`     |
| **chore/**    | 설정 변경, 스크립트 작업 | `chore/docker-setup`         |

### 브랜치 네이밍 규칙

```
타입/이슈번호-간단한-설명
```

### 브랜치 예시

```bash
feat/29-device-registration
fix/45-gps-update-error
refactor/72-service-layer
docs/README-update
```

### Workflow

1. 이슈 생성
2. 해당 이슈 번호로 브랜치 생성
3. 작업 후 커밋 (커밋 컨벤션 준수)
4. Pull Request 생성
5. 코드 리뷰 후 머지

---

## 🔐 보안 고려사항

### 환경 변수 관리

- `.env` 파일은 **절대 커밋하지 않음**
- `.gitignore`에 포함되어 있음
- 민감 정보는 환경 변수로만 관리

### JWT 토큰

- Access Token: 1시간 유효
- Refresh Token: 7일 유효
- Secret Key는 최소 256비트 이상

### 비밀번호

- BCrypt로 암호화 저장
- Salt 자동 생성

---

## 📞 문의 및 지원

- **Repository**: https://github.com/BaroYouFi2025/Baro
- **Issues**: https://github.com/BaroYouFi2025/Baro/issues

---

## 📄 라이선스

이 프로젝트는 BaroYouFi2025 팀의 소유입니다.

---

## 🙏 기여자

BaroYouFi2025 팀원 전체

---

**Last Updated**: 2025-10-20
