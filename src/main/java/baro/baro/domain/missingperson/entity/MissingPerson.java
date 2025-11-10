package baro.baro.domain.missingperson.entity;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import baro.baro.domain.common.enums.GenderType;

@Entity
@Table(name = "missing_persons", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingPerson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    private GenderType gender;
    
    private Integer height;
    
    private Integer weight;
    
    @Column(columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "body_etc", columnDefinition = "TEXT")
    private String bodyEtc;
    
    @Column(name = "clothes_top", columnDefinition = "TEXT")
    private String clothesTop;
    
    @Column(name = "clothes_bottom", columnDefinition = "TEXT")
    private String clothesBottom;
    
    @Column(name = "clothes_etc", columnDefinition = "TEXT")
    private String clothesEtc;
    
    @Column(name = "missing_date", nullable = false)
    private ZonedDateTime missingDate;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(columnDefinition = "geography(Point,4326)")
    private String location;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}