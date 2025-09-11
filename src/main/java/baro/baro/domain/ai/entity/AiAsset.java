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

@Entity
@Table(name = "ai_assets", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAsset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    
    @Column(name = "asset_url", nullable = false, columnDefinition = "TEXT")
    private String assetUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}