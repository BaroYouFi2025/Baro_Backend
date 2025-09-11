package baro.baro.domain.image.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.time.ZonedDateTime;

@Entity
@Table(name = "person_media", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;
    
    @Column(name = "media_url", nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;
    
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private ZonedDateTime uploadedAt;
}