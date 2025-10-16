package baro.baro.domain.member.exception;

import lombok.Getter;

@Getter
public enum MemberErrorCode {
    NOT_CORRECT_INVITEE(403, "Not correct invitee"),
    STATUS_IS_NOT_PENDING(400, "Status is not pending"),
    INVITATION_NOT_FOUND(404, "Invitation not found"),
    RELATIONSHIP_NOT_FOUND(404, "Relationship not found");
    private final int status;
    private final String message;

    MemberErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
