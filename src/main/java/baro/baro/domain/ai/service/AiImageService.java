package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;

// AI 이미지 생성 서비스 인터페이스
// 실종자 정보를 기반으로 AI 이미지를 생성하는 서비스
public interface AiImageService {

    // AI 이미지 생성 (성장/노화 또는 인상착의)
    // - 인상착의 이미지는 생성 즉시 MissingPerson에 자동 저장됨
    GenerateAiImageResponse generateImage(GenerateAiImageRequest request);

    // 성장/노화 이미지만 선택하여 MissingPerson 대표 이미지로 적용
    // - 인상착의 이미지는 이 메서드 사용 불가 (생성 시 자동 저장됨)
    ApplyAiImageResponse applySelectedImage(ApplyAiImageRequest request);
}
