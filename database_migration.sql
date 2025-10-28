-- Firebase 푸시 알림 기능을 위한 데이터베이스 마이그레이션
-- 실행 전 백업 권장

-- 1. devices 테이블에 fcm_token 컬럼 추가
ALTER TABLE youfi.devices 
ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(500);

-- 2. notifications 테이블 생성
CREATE TABLE IF NOT EXISTS youfi.notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES youfi.users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200),
    message VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- 3. 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON youfi.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON youfi.notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON youfi.notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON youfi.notifications(is_read);

-- 4. fcm_token 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_devices_fcm_token ON youfi.devices(fcm_token);

-- 5. 코멘트 추가
COMMENT ON COLUMN youfi.devices.fcm_token IS 'Firebase Cloud Messaging 토큰 (푸시 알림용)';
COMMENT ON TABLE youfi.notifications IS '사용자 알림 이력 테이블';
COMMENT ON COLUMN youfi.notifications.type IS '알림 타입 (INVITE_REQUEST, FOUND_REPORT, NEARBY_ALERT)';
COMMENT ON COLUMN youfi.notifications.is_read IS '읽음 여부';
