package baro.baro.domain.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.time.ZonedDateTime;

// AI 생성 에셋 엔티티
// AI를 통해 생성된 이미지 정보를 저장
@Entity
@Table(name = "ai_assets", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAsset {

    // AI 에셋 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실종자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;

    // 에셋 타입 (AGE_PROGRESSION 또는 GENERATED_IMAGE)
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    // 에셋 URL
    @Column(name = "asset_url", nullable = false, columnDefinition = "TEXT")
    private String assetUrl;

    // 순서 (0, 1, 2, 3...)
    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    // 생성 일시
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}