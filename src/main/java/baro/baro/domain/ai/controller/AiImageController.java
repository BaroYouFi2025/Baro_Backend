package baro.baro.domain.ai.controller;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;
import baro.baro.domain.ai.service.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AI 이미지 생성 컨트롤러
// 실종자 정보를 기반으로 AI 이미지를 생성하는 REST API를 제공
// Google GenAI (Gemini) API를 활용하여 성장/노화 이미지 또는 인상착의 이미지를 생성
//
// Base URL: /ai/images
//
// 제공 기능:
// - 성장/노화 이미지 생성: 정면 사진 4장
// - 인상착의 이미지 생성: 의상 및 외모 정보 기반 1장
//
// 인증: JWT 토큰 필요 (Authorization: Bearer {token})
@RestController
@RequestMapping("/ai/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Image", description = "AI 이미지 생성 API")
public class AiImageController {

    private final AiImageService aiImageService;

    // AI 이미지 생성
    // 실종자 정보를 기반으로 AI 이미지를 생성
    // 에셋 타입에 따라 생성되는 이미지 개수가 달라짐
    //
    // 에셋 타입별 생성 개수:
    // - AGE_PROGRESSION: 4장 (정면 사진 4개 변형)
    // - GENERATED_IMAGE: 1장 (인상착의 기반 전신 이미지)
    //
    // 요청 예시:
    // POST /ai/images/generate
    // {
    //   "missingPersonId": 1,
    //   "assetType": "AGE_PROGRESSION"
    // }
    //
    // 응답 예시:
    // {
    //   "assetType": "AGE_PROGRESSION",
    //   "imageUrls": [
    //     "http://localhost:8080/images/ai/image1.png",
    //     "http://localhost:8080/images/ai/image2.png",
    //     "http://localhost:8080/images/ai/image3.png",
    //     "http://localhost:8080/images/ai/image4.png"
    //   ]
    // }
    //
    @PostMapping("/generate")
    @Operation(summary = "AI 이미지 생성", description = "실종자 정보 기반으로 AI 이미지를 생성합니다. (성장/노화: 3장, 인상착의: 1장)")
    public ResponseEntity<GenerateAiImageResponse> generateAiImage(
            @Valid @RequestBody GenerateAiImageRequest request) {

        log.info("AI 이미지 생성 API 호출 - MissingPersonId: {}, AssetType: {}",
                request.getMissingPersonId(), request.getAssetType());

        GenerateAiImageResponse response = aiImageService.generateImage(request);

        return ResponseEntity.ok(response);
    }

    // 생성된 AI 이미지 중 하나를 선택하여 MissingPerson의 대표 이미지로 적용
    //
    // 요청 예시:
    // POST /ai/images/apply
    // {
    //   "missingPersonId": 1,
    //   "assetType": "AGE_PROGRESSION",
    //   "sequenceOrder": 1
    // }
    @PostMapping("/apply")
    @Operation(summary = "AI 이미지 적용", description = "생성된 이미지 중 선택한 이미지를 MissingPerson에 대표 이미지로 저장합니다.")
    public ResponseEntity<ApplyAiImageResponse> applyAiImage(@Valid @RequestBody ApplyAiImageRequest request) {
        ApplyAiImageResponse response = aiImageService.applySelectedImage(request);
        return ResponseEntity.ok(response);
    }
}
