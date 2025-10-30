package baro.baro.domain.ai.dto.req;

import baro.baro.domain.common.enums.AssetType;
import lombok.Data;

/**
 * AI 이미지 생성 요청 DTO
 *
 * <p>실종자 정보를 기반으로 AI 이미지를 생성하기 위한 요청 데이터를 담습니다.
 * 성장/노화 이미지 또는 인상착의 기반 이미지 생성을 요청할 수 있습니다.</p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>
 * GenerateAiImageRequest request = GenerateAiImageRequest.create(1L, AssetType.AGE_PROGRESSION);
 * </pre>
 */
@Data
public class GenerateAiImageRequest {

    /**
     * 실종자 ID
     * AI 이미지를 생성할 대상 실종자의 데이터베이스 ID
     */
    private Long missingPersonId;

    /**
     * 에셋 타입
     * AGE_PROGRESSION (성장/노화 이미지) 또는 DESCRIPTION (인상착의 기반 이미지)
     */
    private AssetType assetType;

    /**
     * 요청 생성 팩토리 메서드
     *
     * <p>실종자 ID와 에셋 타입을 지정하여 AI 이미지 생성 요청 객체를 생성합니다.</p>
     *
     * @param missingPersonId 실종자 ID
     * @param assetType 생성할 이미지 타입 (AGE_PROGRESSION 또는 DESCRIPTION)
     * @return 생성된 GenerateAiImageRequest 객체
     */
    public static GenerateAiImageRequest create(Long missingPersonId, AssetType assetType) {
        GenerateAiImageRequest request = new GenerateAiImageRequest();
        request.missingPersonId = missingPersonId;
        request.assetType = assetType;
        return request;
    }
}
