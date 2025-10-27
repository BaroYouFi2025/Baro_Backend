#!/bin/bash

echo "🧪 Baro 푸시 알림 테스트 시뮬레이션"
echo "=================================="

# 테스트 환경 변수
BASE_URL="http://localhost:8080"
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
FCM_TOKEN="fcm-token-from-web-browser"
FIREBASE_SERVER_KEY="your-firebase-server-key"

echo ""
echo "📋 테스트 시나리오:"
echo "1. 사용자 로그인"
echo "2. 기기 등록 (FCM 토큰 포함)"
echo "3. 구성원 초대 요청 (푸시 알림 발송)"
echo "4. 초대 수락 (푸시 알림 발송)"
echo "5. FCM 직접 테스트"

echo ""
echo "🔧 테스트 준비사항:"
echo "- 서버 실행: ./gradlew bootRun"
echo "- Firebase 키 파일 배치: src/main/resources/firebase-adminsdk.json"
echo "- 환경변수 설정: FIREBASE_PROJECT_ID=baroyofi"

echo ""
echo "📱 Postman 테스트 방법:"
echo "1. Baro_Push_Notification_Test.postman_collection.json Import"
echo "2. 환경변수 설정:"
echo "   - base_url: $BASE_URL"
echo "   - jwt_token: $JWT_TOKEN"
echo "   - fcm_token: $FCM_TOKEN"
echo "   - firebase_server_key: $FIREBASE_SERVER_KEY"
echo "3. 테스트 시나리오 순서대로 실행"

echo ""
echo "🔥 FCM 토큰 발급 방법:"
echo "1. 웹 브라우저에서 fcm-test.html 열기"
echo "2. Firebase 설정 정보 입력"
echo "3. 'Get FCM Token' 버튼 클릭"
echo "4. 발급받은 토큰을 Postman에 입력"

echo ""
echo "🔑 Firebase Server Key 발급:"
echo "1. Firebase Console > baroyofi 프로젝트"
echo "2. 프로젝트 설정 > 클라우드 메시징"
echo "3. 서버 키 복사"
echo "4. Postman 환경변수에 입력"

echo ""
echo "✅ 테스트 완료 후 확인사항:"
echo "- 푸시 알림 수신 확인"
echo "- 알림 이력 DB 저장 확인"
echo "- 로그에서 Firebase 초기화 확인"

echo ""
echo "🚀 실제 테스트를 위해 서버를 실행하고 Postman을 사용하세요!"
