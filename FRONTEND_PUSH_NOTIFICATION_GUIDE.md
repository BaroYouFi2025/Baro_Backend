# 📱 프론트엔드 푸시 알림 액션 처리 가이드

## 🎯 개요
푸시 알림에서 직접 "수락"/"거절" 버튼을 클릭하여 API를 호출하는 방법입니다.

## 🔧 구현 방법

### 1. Firebase 푸시 알림 설정

#### **Android (Kotlin/Java)**
```kotlin
// FirebaseMessagingService.kt
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // 액션 버튼이 포함된 알림 생성
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        
        if (data["type"] == "invitation") {
            showInvitationNotification(notification, data)
        }
    }
    
    private fun showInvitationNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        
        // 수락 액션
        val acceptIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACCEPT_INVITATION"
            putExtra("invitationId", data["invitationId"])
            putExtra("relation", data["relation"])
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        
        // 거절 액션
        val rejectIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "REJECT_INVITATION"
            putExtra("invitationId", data["invitationId"])
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(this, 2, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        
        val notificationBuilder = NotificationCompat.Builder(this, "invitation_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification?.title)
            .setContentText(notification?.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check, "수락", acceptPendingIntent)
            .addAction(R.drawable.ic_close, "거절", rejectPendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }
}
```

#### **iOS (Swift)**
```swift
// AppDelegate.swift
import UserNotifications

func userNotificationCenter(_ center: UNUserNotificationCenter, 
                          didReceive response: UNNotificationResponse, 
                          withCompletionHandler completionHandler: @escaping () -> Void) {
    
    let userInfo = response.notification.request.content.userInfo
    
    if let type = userInfo["type"] as? String, type == "invitation" {
        switch response.actionIdentifier {
        case "ACCEPT_ACTION":
            handleInvitationAccept(userInfo: userInfo)
        case "REJECT_ACTION":
            handleInvitationReject(userInfo: userInfo)
        default:
            break
        }
    }
    
    completionHandler()
}

func handleInvitationAccept(userInfo: [AnyHashable: Any]) {
    guard let invitationId = userInfo["invitationId"] as? String,
          let relation = userInfo["relation"] as? String else { return }
    
    // API 호출
    acceptInvitation(invitationId: invitationId, relation: relation)
}

func handleInvitationReject(userInfo: [AnyHashable: Any]) {
    guard let invitationId = userInfo["invitationId"] as? String else { return }
    
    // API 호출
    rejectInvitation(invitationId: invitationId)
}
```

### 2. 웹 (JavaScript)

```javascript
// service-worker.js
self.addEventListener('notificationclick', function(event) {
    event.notification.close();
    
    const data = event.notification.data;
    
    if (data.type === 'invitation') {
        if (event.action === 'accept') {
            // 수락 API 호출
            fetch('/api/members/invitations/accept', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + getToken()
                },
                body: JSON.stringify({
                    invitationId: data.invitationId,
                    relation: data.relation
                })
            });
        } else if (event.action === 'reject') {
            // 거절 API 호출
            fetch('/api/members/invitations/reject', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + getToken()
                },
                body: JSON.stringify({
                    invitationId: data.invitationId
                })
            });
        }
    }
});
```

### 3. React Native

```javascript
// PushNotificationHandler.js
import messaging from '@react-native-firebase/messaging';
import { Alert } from 'react-native';

// 백그라운드 메시지 처리
messaging().setBackgroundMessageHandler(async remoteMessage => {
    if (remoteMessage.data.type === 'invitation') {
        // 액션 버튼이 포함된 로컬 알림 생성
        showInvitationNotification(remoteMessage);
    }
});

// 포그라운드 메시지 처리
messaging().onMessage(async remoteMessage => {
    if (remoteMessage.data.type === 'invitation') {
        Alert.alert(
            remoteMessage.notification.title,
            remoteMessage.notification.body,
            [
                {
                    text: '거절',
                    onPress: () => rejectInvitation(remoteMessage.data.invitationId)
                },
                {
                    text: '수락',
                    onPress: () => acceptInvitation(remoteMessage.data.invitationId, remoteMessage.data.relation)
                }
            ]
        );
    }
});

const acceptInvitation = async (invitationId, relation) => {
    try {
        const response = await fetch('https://your-api.com/members/invitations/accept', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${await getToken()}`
            },
            body: JSON.stringify({
                invitationId: parseInt(invitationId),
                relation: relation
            })
        });
        
        if (response.ok) {
            Alert.alert('성공', '초대를 수락했습니다.');
        }
    } catch (error) {
        Alert.alert('오류', '초대 수락에 실패했습니다.');
    }
};

const rejectInvitation = async (invitationId) => {
    try {
        const response = await fetch('https://your-api.com/members/invitations/reject', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${await getToken()}`
            },
            body: JSON.stringify({
                invitationId: parseInt(invitationId)
            })
        });
        
        if (response.ok) {
            Alert.alert('성공', '초대를 거절했습니다.');
        }
    } catch (error) {
        Alert.alert('오류', '초대 거절에 실패했습니다.');
    }
};
```

## 📊 푸시 알림 데이터 구조

### **서버에서 발송하는 데이터:**
```json
{
  "type": "invitation",
  "invitationId": "1",
  "inviterName": "홍길동",
  "relation": "가족",
  "actions": "[\"accept\", \"reject\"]",
  "acceptUrl": "https://your-app.com/api/members/invitations/accept",
  "rejectUrl": "https://your-app.com/api/members/invitations/reject"
}
```

### **API 요청 형식:**
```json
// 수락
POST /members/invitations/accept
{
  "invitationId": 1,
  "relation": "가족"
}

// 거절
POST /members/invitations/reject
{
  "invitationId": 1
}
```

## 🎯 사용자 경험

1. **푸시 알림 수신** → "홍길동님이 가족으로 초대했습니다"
2. **알림에서 직접 버튼 클릭** → "수락" 또는 "거절"
3. **API 자동 호출** → 서버에서 처리
4. **결과 알림** → "초대가 수락되었습니다" 또는 "초대가 거절되었습니다"

## ⚠️ 주의사항

1. **인증 토큰**: API 호출 시 JWT 토큰 필요
2. **오프라인 처리**: 네트워크 연결 상태 확인
3. **에러 처리**: API 호출 실패 시 재시도 로직
4. **보안**: 액션 버튼 클릭 시 사용자 확인

이렇게 구현하면 사용자가 푸시 알림에서 직접 수락/거절할 수 있습니다! 🚀
