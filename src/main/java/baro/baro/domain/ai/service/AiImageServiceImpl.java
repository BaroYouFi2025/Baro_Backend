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
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

/**
 * AI 이미지 생성 서비스 구현체
 *
 * <p>AiImageService 인터페이스의 구현체입니다.
 * Google GenAI API를 활용하여 실종자 정보 기반 AI 이미지를 생성하고
 * 생성된 이미지 정보를 데이터베이스에 저장합니다.</p>
 *
 * <p><b>주요 책임:</b></p>
 * <ul>
 *   <li>실종자 정보 조회 및 검증</li>
 *   <li>Google GenAI 서비스를 통한 이미지 생성</li>
 *   <li>생성된 이미지 정보의 AiAsset 저장</li>
 *   <li>트랜잭션 관리</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    private final MissingPersonRepository missingPersonRepository;
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

        // 0. 입력 검증
        if (request.getMissingPersonId() == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_ID_REQUIRED);
        }
        if (request.getAssetType() == null) {
            throw new AiException(AiErrorCode.ASSET_TYPE_REQUIRED);
        }

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
                // 성장/노화: 3장 생성
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
            
            for (String url : imageUrls) {
                if (url == null || url.isBlank()) {
                    throw new AiException(AiErrorCode.INVALID_IMAGE_URL);
                }
            }
            
        } catch (AiException e) {
            throw e; // AiException은 그대로 전파
        } catch (Exception e) {
            log.error("이미지 생성 중 예외 발생", e);
            throw new AiException(AiErrorCode.IMAGE_GENERATION_FAILED, e);
        }

        // 3. AiAsset에 저장 (여러 레코드)
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

        log.info("AI 이미지 생성 완료 - 총 {}장 저장됨", savedAssets.size());

        return GenerateAiImageResponse.create(request.getAssetType(), imageUrls);
    }

    /**
     * 선택한 AI 이미지를 MissingPerson 대표 이미지로 적용합니다.
     */
    @Override
    @Transactional
    public ApplyAiImageResponse applySelectedImage(ApplyAiImageRequest request) {
        User currentUser = getCurrentUser(); // 현재 인증된 사용자 정보 조회

        // 0. 입력 검증
        if (request.getMissingPersonId() == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_ID_REQUIRED);
        }
        if (request.getAssetType() == null) {
            throw new AiException(AiErrorCode.ASSET_TYPE_REQUIRED);
        }
        if (request.getSelectedImageUrl() == null || request.getSelectedImageUrl().isBlank()) {
            throw new AiException(AiErrorCode.IMAGE_URL_REQUIRED);
        }

        // 1) MissingCase 조회 및 검증
        MissingCase missingCase = missingCaseRepository.findByMissingPersonId(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_CASE_NOT_FOUND));

        // 2) 접근 권한 검증
        missingCase.getReportedBy() // 신고자 정보 조회
                .validateUserAccess(currentUser); // 접근 권한 검증

        // 3) MissingPerson에 대표 이미지 URL 적용
        MissingPerson missingPerson = missingCase.getMissingPerson();
        
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }
        
        missingPerson.updateAiImage(request.getSelectedImageUrl(), request.getAssetType());

        // JPA 더티체킹으로 자동 반영됨

        return ApplyAiImageResponse.create(
                missingPerson.getId(),
                request.getAssetType(),
                request.getSelectedImageUrl()
        );
    }
}
