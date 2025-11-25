package baro.baro.domain.member.exception;

import lombok.Getter;

@Getter
public enum MemberErrorCode {
    NOT_CORRECT_INVITEE(403, "초대 대상자가 아닙니다"),
    STATUS_IS_NOT_PENDING(400, "대기 중인 상태가 아닙니다"),
    INVITATION_NOT_FOUND(404, "초대를 찾을 수 없습니다"),
    RELATIONSHIP_NOT_FOUND(404, "관계를 찾을 수 없습니다"),
    DUPLICATE_INVITATION(400, "이미 초대가 진행 중이거나 관계가 존재합니다");
    private final int status;
    private final String message;

    MemberErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
