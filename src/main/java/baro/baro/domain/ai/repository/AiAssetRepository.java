package baro.baro.domain.ai.repository;

import baro.baro.domain.ai.entity.AiAsset;
import baro.baro.domain.common.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 에셋 Repository
 *
 * <p>AI로 생성된 에셋(이미지 등)의 데이터베이스 접근을 담당합니다.
 * 실종자별, 에셋 타입별 조회 기능을 제공합니다.</p>
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>실종자별 AI 에셋 조회 (최신순)</li>
 *   <li>실종자 + 에셋 타입별 조회 (순서별)</li>
 *   <li>실종자별 전체 AI 에셋 조회</li>
 * </ul>
 */
@Repository
public interface AiAssetRepository extends JpaRepository<AiAsset, Long> {

    /**
     * 실종자별 모든 AI 이미지 조회 (생성일 기준 내림차순)
     *
     * <p>특정 실종자에 대해 생성된 모든 AI 에셋을 최신순으로 조회합니다.
     * 여러 타입의 에셋이 섞여 있을 수 있습니다.</p>
     *
     * @param missingPersonId 실종자 ID
     * @return 생성일 내림차순 정렬된 AI 에셋 리스트
     */
    List<AiAsset> findByMissingPersonIdOrderByCreatedAtDesc(Long missingPersonId);

    /**
     * 실종자 + 에셋 타입별 조회 (순서 기준 오름차순)
     *
     * <p>특정 실종자의 특정 타입 AI 에셋을 순서대로 조회합니다.
     * AGE_PROGRESSION의 경우 0(현재), 1(+5년), 2(+10년) 순서로 반환됩니다.</p>
     *
     * @param missingPersonId 실종자 ID
     * @param assetType 에셋 타입 (AGE_PROGRESSION 또는 DESCRIPTION)
     * @return 순서 오름차순 정렬된 AI 에셋 리스트
     */
    List<AiAsset> findByMissingPersonIdAndAssetTypeOrderBySequenceOrderAsc(Long missingPersonId, AssetType assetType);

    /**
     * 실종자별 모든 AI 이미지 조회
     *
     * <p>특정 실종자에 대해 생성된 모든 AI 에셋을 조회합니다.
     * 정렬 순서는 데이터베이스 기본값을 따릅니다.</p>
     *
     * @param missingPersonId 실종자 ID
     * @return AI 에셋 리스트
     */
    List<AiAsset> findByMissingPersonId(Long missingPersonId);

    /**
     * 실종자 + 타입 + 순서로 단일 에셋 조회
     *
     * @param missingPersonId 실종자 ID
     * @param assetType 에셋 타입
     * @param sequenceOrder 순서 (0,1,2)
     * @return 일치하는 AI 에셋
     */
    Optional<AiAsset> findByMissingPersonIdAndAssetTypeAndSequenceOrder(Long missingPersonId, AssetType assetType, Integer sequenceOrder);
}
