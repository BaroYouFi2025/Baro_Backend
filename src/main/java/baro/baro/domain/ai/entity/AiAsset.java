package baro.baro.domain.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.common.enums.AssetType;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.time.ZonedDateTime;

/**
 * AI 생성 에셋 엔티티
 *
 * <p>AI를 통해 생성된 이미지 등의 에셋 정보를 저장합니다.
 * 실종자 정보를 기반으로 생성된 성장/노화 이미지나 인상착의 이미지를 관리합니다.</p>
 *
 * <p><b>테이블 정보:</b></p>
 * <ul>
 *   <li>테이블명: ai_assets</li>
 *   <li>스키마: youfi</li>
 *   <li>연관관계: MissingPerson (N:1)</li>
 * </ul>
 *
 * <p><b>에셋 타입별 저장 개수:</b></p>
 * <ul>
 *   <li>AGE_PROGRESSION: 3개 (현재, +5년, +10년)</li>
 *   <li>DESCRIPTION: 1개 (인상착의 기반)</li>
 * </ul>
 */
@Entity
@Table(name = "ai_assets", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAsset {

    /**
     * AI 에셋 고유 ID
     * 자동 증가 (AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 실종자 정보
     * 이 AI 에셋이 생성된 대상 실종자 (지연 로딩)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;

    /**
     * 에셋 타입
     * AGE_PROGRESSION (성장/노화) 또는 DESCRIPTION (인상착의)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    /**
     * 에셋 URL
     * 생성된 이미지가 저장된 외부 스토리지 URL
     */
    @Column(name = "asset_url", nullable = false, columnDefinition = "TEXT")
    private String assetUrl;

    /**
     * 순서
     * 같은 타입의 여러 에셋 중 순서를 나타냄 (0, 1, 2 등)
     * AGE_PROGRESSION의 경우: 0=현재, 1=+5년, 2=+10년
     */
    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    /**
     * 생성 일시
     * 에셋이 생성된 시각 (자동 설정)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}