package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;
import baro.baro.domain.ai.entity.AiAsset;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.repository.AiAssetRepository;
import baro.baro.domain.common.enums.AssetType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    private final MissingCaseRepository missingCaseRepository;
    private final AiAssetRepository aiAssetRepository;
    private final GoogleGenAiService googleGenAiService;
    
    /**
     * AI 이미지 생성
     * @param request 이미지 생성 요청 (실종자 ID, 에셋 타입)
     * @return 생성된 이미지 URL 목록
     * @throws MissingPersonException 실종자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public GenerateAiImageResponse generateImage(GenerateAiImageRequest request) {
        User currentUser = getCurrentUser(); // 현재 인증된 사용자 정보 조회

        log.info("AI 이미지 생성 요청 - MissingPersonId: {}, AssetType: {}",
                request.getMissingPersonId(), request.getAssetType());

        // 1. MissingPerson 조회 및 검증
        MissingCase missingCase = missingCaseRepository.findByMissingPersonId(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_CASE_NOT_FOUND));

        // 2. 접근 권한 검증
        missingCase.getReportedBy()
                .validateUserAccess(currentUser);

        // 3. MissingPerson 정보 가져오기
        MissingPerson missingPerson = missingCase.getMissingPerson();
        
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }

        // 4. Google GenAI로 이미지 생성
        List<String> imageUrls;
        try {
            if (request.getAssetType() == AssetType.AGE_PROGRESSION) {
                // 성장/노화: 4장 생성
                imageUrls = googleGenAiService.generateAgeProgressionImages(missingPerson);
            } else {
                // 인상착의: 1장 생성
                String imageUrl = googleGenAiService.generateDescriptionImage(missingPerson);
                imageUrls = List.of(imageUrl);
            }

            // 생성된 이미지 검증
            if (imageUrls == null || imageUrls.isEmpty()) {
                throw new AiException(AiErrorCode.EMPTY_RESPONSE);
            }

        } catch (AiException e) {
            throw e; // AiException은 그대로 전파
        } catch (Exception e) {
            log.error("이미지 생성 중 예외 발생", e);
            throw new AiException(AiErrorCode.IMAGE_GENERATION_FAILED);
        }

        // 5. AiAsset에 저장 (여러 레코드)
        List<AiAsset> savedAssets = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            AiAsset asset = AiAsset.builder()
                    .missingPerson(missingPerson)
                    .assetType(request.getAssetType())
                    .assetUrl(imageUrls.get(i))
                    .sequenceOrder(i) // 순서 저장 (0, 1, 2...)
                    .build();
            savedAssets.add(aiAssetRepository.save(asset));
        }

        // 6. 인상착의 이미지는 즉시 MissingPerson 대표 이미지로 저장
        if (request.getAssetType() == AssetType.GENERATED_IMAGE) {
            missingPerson.updateAiImage(imageUrls.get(0), AssetType.GENERATED_IMAGE);
            log.info("인상착의 이미지 MissingPerson에 자동 적용 완료 - URL: {}", imageUrls.get(0));
        }

        log.info("AI 이미지 생성 완료 - 총 {}장 저장됨", savedAssets.size());

        return GenerateAiImageResponse.create(request.getAssetType(), imageUrls);
    }

    /**
     * 선택한 성장/노화 이미지를 MissingPerson 대표 이미지로 적용
     * (인상착의 이미지는 생성 시 자동 저장되므로 이 메서드에서는 처리하지 않음)
     */
    @Override
    @Transactional
    public ApplyAiImageResponse applySelectedImage(ApplyAiImageRequest request) {
        User currentUser = getCurrentUser(); // 현재 인증된 사용자 정보 조회

        // 1) AssetType 검증 - 성장/노화 이미지만 처리
        if (request.getAssetType() != AssetType.AGE_PROGRESSION) {
            throw new AiException(AiErrorCode.INVALID_ASSET_TYPE);
        }

        // 2) MissingCase 조회 및 검증
        MissingCase missingCase = missingCaseRepository.findByMissingPersonId(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_CASE_NOT_FOUND));

        // 3) 접근 권한 검증
        missingCase.getReportedBy() // 신고자 정보 조회
                .validateUserAccess(currentUser); // 접근 권한 검증

        // 4) MissingPerson에 대표 이미지 URL 적용
        MissingPerson missingPerson = missingCase.getMissingPerson();

        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }

        missingPerson.updateAiImage(request.getSelectedImageUrl(), request.getAssetType());

        // JPA 더티체킹으로 자동 반영됨

        log.info("성장/노화 이미지 적용 완료 - MissingPersonId: {}, URL: {}",
                missingPerson.getId(), request.getSelectedImageUrl());

        return ApplyAiImageResponse.create(
                missingPerson.getId(),
                request.getAssetType(),
                request.getSelectedImageUrl()
        );
    }
}
