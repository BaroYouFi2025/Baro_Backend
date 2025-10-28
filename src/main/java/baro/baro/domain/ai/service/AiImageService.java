package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;

/**
 * AI 이미지 생성 서비스 인터페이스
 *
 * <p>실종자 정보를 기반으로 AI 이미지를 생성하는 서비스의 인터페이스를 정의합니다.
 * 성장/노화 이미지 또는 인상착의 기반 이미지 생성 기능을 제공합니다.</p>
 *
 * <p><b>지원 기능:</b></p>
 * <ul>
 *   <li>성장/노화 이미지 생성 (AGE_PROGRESSION): 실종 당시 사진 기반 현재 나이 예측 (3가지 스타일/각도)</li>
 *   <li>인상착의 이미지 생성 (DESCRIPTION): 의상 및 외모 정보 기반 1장</li>
 * </ul>
 */
public interface AiImageService {

    /**
     * AI 이미지 생성 (성장/노화 또는 인상착의)
     *
     * <p>실종자 정보를 기반으로 Google GenAI API를 통해 AI 이미지를 생성합니다.
     * 생성된 이미지는 AiAsset 테이블에 저장되며, 이미지 URL 목록을 반환합니다.</p>
     *
     * <p><b>처리 흐름:</b></p>
     * <ol>
     *   <li>실종자 정보 조회</li>
     *   <li>에셋 타입에 따라 Google GenAI API 호출</li>
     *   <li>생성된 이미지 URL을 AiAsset에 저장</li>
     *   <li>이미지 URL 목록 반환</li>
     * </ol>
     *
     * @param request 이미지 생성 요청 (실종자 ID, 에셋 타입)
     * @return 생성된 이미지 URL 목록
     * @throws baro.baro.domain.missingperson.exception.MissingPersonException 실종자를 찾을 수 없는 경우
     */
    GenerateAiImageResponse generateImage(GenerateAiImageRequest request);

    /**
     * 선택한 AI 이미지를 MissingPerson 대표 이미지로 적용
     *
     * @param request 적용 요청 (실종자 ID, 에셋 타입, 순서)
     * @return 적용 결과 (적용된 URL 포함)
     */
    ApplyAiImageResponse applySelectedImage(ApplyAiImageRequest request);
}
