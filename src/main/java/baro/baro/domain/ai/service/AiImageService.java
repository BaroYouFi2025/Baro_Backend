package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;

// AI 이미지 생성 서비스 인터페이스
// 실종자 정보를 기반으로 AI 이미지를 생성하는 서비스
public interface AiImageService {

    // AI 이미지 생성 (성장/노화 또는 인상착의)
    GenerateAiImageResponse generateImage(GenerateAiImageRequest request);

    // 선택한 AI 이미지를 MissingPerson 대표 이미지로 적용
    ApplyAiImageResponse applySelectedImage(ApplyAiImageRequest request);
}
