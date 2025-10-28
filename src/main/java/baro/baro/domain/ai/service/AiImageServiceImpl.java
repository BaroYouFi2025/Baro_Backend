package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;
import baro.baro.domain.ai.entity.AiAsset;
import baro.baro.domain.ai.repository.AiAssetRepository;
import baro.baro.domain.common.enums.AssetType;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final AiAssetRepository aiAssetRepository;
    private final GoogleGenAiService googleGenAiService;
    
    /**
     * AI 이미지 생성
     *
     * <p>실종자 정보를 기반으로 AI 이미지를 생성하고 데이터베이스에 저장합니다.</p>
     *
     * <p><b>처리 과정:</b></p>
     * <ol>
     *   <li>실종자 정보 조회 및 검증</li>
     *   <li>에셋 타입에 따라 Google GenAI 서비스 호출
     *       <ul>
     *         <li>AGE_PROGRESSION: 3장 (실종 당시 사진 기반 현재 나이 예측, 3가지 스타일/각도)</li>
     *         <li>DESCRIPTION: 1장 (인상착의 기반)</li>
     *       </ul>
     *   </li>
     *   <li>생성된 이미지 URL들을 AiAsset 테이블에 순서대로 저장</li>
     *   <li>응답 DTO 생성 및 반환</li>
     * </ol>
     *
     * @param request 이미지 생성 요청 (실종자 ID, 에셋 타입)
     * @return 생성된 이미지 URL 목록
     * @throws MissingPersonException 실종자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public GenerateAiImageResponse generateImage(GenerateAiImageRequest request) {
        log.info("AI 이미지 생성 요청 - MissingPersonId: {}, AssetType: {}",
                request.getMissingPersonId(), request.getAssetType());

        // 1. 실종자 조회 및 검증
        MissingPerson missingPerson = missingPersonRepository.findById(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 2. Google GenAI로 이미지 생성
        List<String> imageUrls;
        if (request.getAssetType() == AssetType.AGE_PROGRESSION) {
            // 성장/노화: 3장 생성
            imageUrls = googleGenAiService.generateAgeProgressionImages(missingPerson);
        } else {
            // 인상착의: 1장 생성
            String imageUrl = googleGenAiService.generateDescriptionImage(missingPerson);
            imageUrls = List.of(imageUrl);
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
        // 1) 실종자 검증
        MissingPerson missingPerson = missingPersonRepository.findById(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 2) 선택한 AI 에셋 조회 (순서 기반)
        Optional<AiAsset> assetOpt = aiAssetRepository.findByMissingPersonIdAndAssetTypeAndSequenceOrder(
                request.getMissingPersonId(), request.getAssetType(), request.getSequenceOrder());

        AiAsset asset = assetOpt.orElseThrow(() ->
                new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 3) MissingPerson에 대표 이미지 URL 적용
        missingPerson.updateAiImage(asset.getAssetUrl(), request.getAssetType());

        // JPA 더티체킹으로 자동 반영됨

        return ApplyAiImageResponse.create(
                missingPerson.getId(),
                request.getAssetType(),
                request.getSequenceOrder(),
                asset.getAssetUrl()
        );
    }
}
