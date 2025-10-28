package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Nanobanana API 응답 DTO
 *
 * <p>Nanobanana API로부터 받은 이미지 생성 결과를 담는 DTO입니다.
 * Base64 인코딩된 이미지 데이터 또는 이미지 URL을 포함합니다.</p>
 */
@Data
public class NanobananaResponse {

    /**
     * 생성된 이미지 리스트
     */
    @JsonProperty("images")
    private List<GeneratedImage> images;

    /**
     * 요청 처리 시간 (초)
     */
    @JsonProperty("processing_time")
    private Double processingTime;

    /**
     * 사용된 seed 값
     */
    @JsonProperty("seed")
    private Long seed;

    /**
     * 에러 메시지 (실패 시)
     */
    @JsonProperty("error")
    private String error;

    /**
     * GeneratedImage 클래스
     * 생성된 개별 이미지 정보
     */
    @Data
    public static class GeneratedImage {
        /**
         * Base64 인코딩된 이미지 데이터
         */
        @JsonProperty("base64")
        private String base64;

        /**
         * 이미지 URL (스토리지에 저장된 경우)
         */
        @JsonProperty("url")
        private String url;

        /**
         * 이미지 크기
         */
        @JsonProperty("size")
        private String size;

        /**
         * MIME 타입
         */
        @JsonProperty("mime_type")
        private String mimeType;
    }

    /**
     * 첫 번째 이미지의 Base64 데이터 추출
     *
     * @return Base64 인코딩된 이미지 문자열, 없으면 null
     */
    public String getFirstImageBase64() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getBase64();
    }

    /**
     * 첫 번째 이미지의 URL 추출
     *
     * @return 이미지 URL, 없으면 null
     */
    public String getFirstImageUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getUrl();
    }

    /**
     * 성공 여부 확인
     *
     * @return 에러가 없고 이미지가 있으면 true
     */
    public boolean isSuccess() {
        return error == null && images != null && !images.isEmpty();
    }
}
