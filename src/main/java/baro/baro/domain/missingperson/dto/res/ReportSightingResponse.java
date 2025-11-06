package baro.baro.domain.missingperson.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 실종자 발견 신고 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "실종자 발견 신고 응답")
public class ReportSightingResponse {
    
    @Schema(description = "신고 처리 메시지", example = "신고가 접수되었습니다. 실종자 등록자에게 알림이 전송되었습니다.")
    private String message;
    
    /**
     * 기본 성공 메시지로 응답 생성
     */
    public static ReportSightingResponse success() {
        return ReportSightingResponse.builder()
                .message("신고가 접수되었습니다. 실종자 등록자에게 알림이 전송되었습니다.")
                .build();
    }
    
    /**
     * 커스텀 메시지로 응답 생성
     */
    public static ReportSightingResponse of(String message) {
        return ReportSightingResponse.builder()
                .message(message)
                .build();
    }
}

