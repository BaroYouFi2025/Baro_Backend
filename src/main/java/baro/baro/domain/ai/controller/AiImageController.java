package baro.baro.domain.ai.controller;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.service.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 이미지 생성 컨트롤러
 *
 * <p>실종자 정보를 기반으로 AI 이미지를 생성하는 REST API를 제공합니다.
 * Google GenAI (Gemini) API를 활용하여 성장/노화 이미지 또는 인상착의 이미지를 생성합니다.</p>
 *
 * <p><b>Base URL:</b> /ai/images</p>
 *
 * <p><b>제공 기능:</b></p>
 * <ul>
 *   <li>성장/노화 이미지 생성: 현재, +5년, +10년 총 3장</li>
 *   <li>인상착의 이미지 생성: 의상 및 외모 정보 기반 1장</li>
 * </ul>
 *
 * <p><b>인증:</b> JWT 토큰 필요 (Authorization: Bearer {token})</p>
 *
 * @see AiImageService
 * @see GenerateAiImageRequest
 * @see GenerateAiImageResponse
 */
@RestController
@RequestMapping("/ai/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Image", description = "AI 이미지 생성 API")
public class AiImageController {

    private final AiImageService aiImageService;

    /**
     * AI 이미지 생성
     *
     * <p>실종자 정보를 기반으로 AI 이미지를 생성합니다.
     * 에셋 타입에 따라 생성되는 이미지 개수가 달라집니다.</p>
     *
     * <p><b>에셋 타입별 생성 개수:</b></p>
     * <ul>
     *   <li>AGE_PROGRESSION: 3장 (현재 나이, +5년, +10년)</li>
     *   <li>DESCRIPTION: 1장 (인상착의 기반 전신 이미지)</li>
     * </ul>
     *
     * <p><b>요청 예시:</b></p>
     * <pre>
     * POST /ai/images/generate
     * {
     *   "missingPersonId": 1,
     *   "assetType": "AGE_PROGRESSION"
     * }
     * </pre>
     *
     * <p><b>응답 예시:</b></p>
     * <pre>
     * {
     *   "assetType": "AGE_PROGRESSION",
     *   "imageUrls": [
     *     "https://storage.googleapis.com/baro-ai-assets/image1.jpg",
     *     "https://storage.googleapis.com/baro-ai-assets/image2.jpg",
     *     "https://storage.googleapis.com/baro-ai-assets/image3.jpg"
     *   ]
     * }
     * </pre>
     *
     * @param request 이미지 생성 요청 (실종자 ID, 에셋 타입)
     * @return 생성된 이미지 URL 목록
     */
    @PostMapping("/generate")
    @Operation(summary = "AI 이미지 생성", description = "실종자 정보 기반으로 AI 이미지를 생성합니다. (성장/노화: 3장, 인상착의: 1장)")
    public ResponseEntity<GenerateAiImageResponse> generateAiImage(
            @RequestBody GenerateAiImageRequest request) {

        log.info("AI 이미지 생성 API 호출 - MissingPersonId: {}, AssetType: {}",
                request.getMissingPersonId(), request.getAssetType());

        GenerateAiImageResponse response = aiImageService.generateImage(request);

        return ResponseEntity.ok(response);
    }
}
