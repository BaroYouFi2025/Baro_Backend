package baro.baro.domain.common.exception;

import baro.baro.domain.auth.exception.EmailException;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.StringJoiner;

/**
 * 전역 예외 처리를 담당하는 핸들러
 * 애플리케이션에서 발생하는 모든 예외를 캐치하여 적절한 HTTP 응답으로 변환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     * @param e BusinessException
     * @return 에러 코드와 메시지를 포함한 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiErrorResponse.of(e.getErrorCode()));
    }

    /**
     * 이메일 관련 예외 처리
     * @param e EmailException
     * @return 이메일 에러 코드와 메시지를 포함한 응답
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ApiErrorResponse> handleEmail(EmailException e) {
        log.error("Email exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getEmailErrorCode().getStatus())
                .body(ApiErrorResponse.of(
                    e.getEmailErrorCode().name(),
                    e.getEmailErrorCode().getMessage()
                ));
    }

    /**
     * 전화번호 인증 관련 예외 처리
     * @param e PhoneVerificationException
     * @return 전화번호 인증 에러 코드와 메시지를 포함한 응답
     */
    @ExceptionHandler(PhoneVerificationException.class)
    public ResponseEntity<ApiErrorResponse> handlePhoneVerification(PhoneVerificationException e) {
        log.warn("Phone verification exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getPhoneErrorCode().getStatus())
                .body(ApiErrorResponse.of(
                    e.getPhoneErrorCode().name(),
                    e.getPhoneErrorCode().getMessage()
                ));
    }

    /**
     * 요청 데이터 검증 예외 처리
     * @param e MethodArgumentNotValidException 또는 BindException
     * @return 검증 실패 필드와 메시지를 포함한 응답
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception e) {
        log.warn("Validation exception occurred: {}", e.getMessage());
        String errorMessage = "입력값 검증에 실패했습니다.";

        // @Valid 어노테이션으로 검증 실패한 경우 상세 메시지 생성
        if (e instanceof MethodArgumentNotValidException ex) {
            StringJoiner joiner = new StringJoiner(", ");
            ex.getBindingResult().getFieldErrors().forEach(error ->
                joiner.add(error.getField() + ": " + error.getDefaultMessage()));
            errorMessage = joiner.toString();
        }

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiErrorResponse.of("VALIDATION_ERROR", errorMessage));
    }

    /**
     * 필수 요청 파라미터 누락 예외 처리
     * @param e MissingServletRequestParameterException
     * @return 필수 파라미터 누락 에러 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter: {}", e.getParameterName());
        String errorMessage = String.format("필수 파라미터가 누락되었습니다: %s (%s)",
            e.getParameterName(), e.getParameterType());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiErrorResponse.of("MISSING_PARAMETER", errorMessage));
    }

    /**
     * 타입 불일치 예외 처리 (예: String을 Integer로 변환 실패)
     * @param e MethodArgumentTypeMismatchException
     * @return 타입 불일치 에러 응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiErrorResponse.of("TYPE_MISMATCH", "잘못된 타입의 값이 입력되었습니다."));
    }

    /**
     * 접근 권한 거부 예외 처리
     * @param e AccessDeniedException
     * @return 403 Forbidden 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(ApiErrorResponse.of(ErrorCode.FORBIDDEN));
    }

    /**
     * 존재하지 않는 엔드포인트 요청 예외 처리
     * @param e NoHandlerFoundException
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoHandlerFoundException e) {
        log.warn("No handler found exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiErrorResponse.of(ErrorCode.NOT_FOUND));
    }

    /**
     * 정적 리소스를 찾을 수 없는 예외 처리 (API 엔드포인트가 없는 경우)
     * @param e NoResourceFoundException
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("No resource found for path: {}", e.getResourcePath());
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiErrorResponse.of("ENDPOINT_NOT_FOUND", "요청하신 API 엔드포인트를 찾을 수 없습니다: " + e.getResourcePath()));
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     * @param e HttpRequestMethodNotSupportedException
     * @return 405 Method Not Allowed 응답
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(405)
                .body(ApiErrorResponse.of("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    /**
     * 데이터베이스 접근 예외 처리
     * @param e DataAccessException
     * @return 데이터베이스 오류 응답
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException e) {
        log.error("Database exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiErrorResponse.of("DATABASE_ERROR", "데이터베이스 오류가 발생했습니다."));
    }

    /**
     * 예상하지 못한 모든 예외 처리 (최후의 방어선)
     * @param e Exception
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }
}