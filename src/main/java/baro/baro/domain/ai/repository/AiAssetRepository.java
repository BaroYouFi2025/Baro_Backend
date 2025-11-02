package baro.baro.domain.ai.repository;

import baro.baro.domain.ai.entity.AiAsset;
import baro.baro.domain.common.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// AI 에셋 Repository
// AI로 생성된 이미지의 데이터베이스 접근 담당
@Repository
public interface AiAssetRepository extends JpaRepository<AiAsset, Long> {

    // 실종자별 모든 AI 이미지 조회 (생성일 기준 내림차순)
    List<AiAsset> findByMissingPersonIdOrderByCreatedAtDesc(Long missingPersonId);

    // 실종자 + 에셋 타입별 조회 (순서 기준 오름차순)
    List<AiAsset> findByMissingPersonIdAndAssetTypeOrderBySequenceOrderAsc(Long missingPersonId, AssetType assetType);

    // 실종자별 모든 AI 이미지 조회
    List<AiAsset> findByMissingPersonId(Long missingPersonId);

    // 실종자 + 타입 + 순서로 단일 에셋 조회
    Optional<AiAsset> findByMissingPersonIdAndAssetTypeAndSequenceOrder(Long missingPersonId, AssetType assetType, Integer sequenceOrder);
}
