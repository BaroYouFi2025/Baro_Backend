package baro.baro.domain.member.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import baro.baro.domain.user.exception.UserErrorCode;

public class MemberException extends BusinessException {
    private final MemberErrorCode memberErrorCode;

    public MemberException(MemberErrorCode memberErrorCode) {
        super(convertToErrorCode(memberErrorCode));
        this.memberErrorCode = memberErrorCode;}


    private static ErrorCode convertToErrorCode(MemberErrorCode memberErrorCode) {
        return switch (memberErrorCode.getStatus()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 403 -> ErrorCode.FORBIDDEN;
            case 400 -> ErrorCode.BAD_REQUEST;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

}
