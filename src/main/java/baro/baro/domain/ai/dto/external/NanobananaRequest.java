package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Google Nanobanana API 요청 DTO
 *
 * <p>Nanobanana는 Google의 이미지 생성 API입니다.
 * 텍스트 프롬프트를 기반으로 고품질 이미지를 생성합니다.</p>
 */
@Data
public class NanobananaRequest {

    /**
     * 이미지 생성 프롬프트
     * 생성하고자 하는 이미지를 설명하는 텍스트
     */
    @JsonProperty("prompt")
    private String prompt;

    /**
     * 부정 프롬프트 (선택사항)
     * 이미지에서 제외하고 싶은 요소들
     */
    @JsonProperty("negative_prompt")
    private String negativePrompt;

    /**
     * 이미지 수 (기본값: 1)
     */
    @JsonProperty("num_images")
    private Integer numImages;

    /**
     * 이미지 크기
     * 예: "512x512", "1024x1024"
     */
    @JsonProperty("size")
    private String size;

    /**
     * 샘플링 스텝 수 (기본값: 20-50)
     * 높을수록 품질이 좋지만 생성 시간이 길어짐
     */
    @JsonProperty("num_inference_steps")
    private Integer numInferenceSteps;

    /**
     * Guidance scale (기본값: 7.5)
     * 프롬프트를 얼마나 충실히 따를지 결정 (1-20)
     */
    @JsonProperty("guidance_scale")
    private Double guidanceScale;

    /**
     * Seed (선택사항)
     * 동일한 결과를 재현하기 위한 랜덤 시드
     */
    @JsonProperty("seed")
    private Long seed;

    /**
     * 요청 생성 팩토리 메서드
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 NanobananaRequest 객체
     */
    public static NanobananaRequest create(String prompt) {
        NanobananaRequest request = new NanobananaRequest();
        request.prompt = prompt;
        request.numImages = 1;
        request.size = "512x512";
        request.numInferenceSteps = 30;
        request.guidanceScale = 7.5;
        return request;
    }

    /**
     * 고품질 요청 생성 팩토리 메서드
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 고품질 설정의 NanobananaRequest 객체
     */
    public static NanobananaRequest createHighQuality(String prompt) {
        NanobananaRequest request = new NanobananaRequest();
        request.prompt = prompt;
        request.numImages = 1;
        request.size = "1024x1024";
        request.numInferenceSteps = 50;
        request.guidanceScale = 7.5;
        request.negativePrompt = "blurry, low quality, distorted, cartoon, animated";
        return request;
    }
}
