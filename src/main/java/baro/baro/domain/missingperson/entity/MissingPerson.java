package baro.baro.domain.missingperson.entity;

import baro.baro.domain.ai.entity.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private GenderType gender;
    
    private Integer height;
    
    private Integer weight;
    
    @Column(columnDefinition = "TEXT")
    private String body; // 체형 (마름, 보통, 통통 등)
    
    @Column(name = "body_etc", columnDefinition = "TEXT")
    private String bodyEtc;
    
    @Column(name = "clothes_top", columnDefinition = "TEXT")
    private String clothesTop;
    
    @Column(name = "clothes_bottom", columnDefinition = "TEXT")
    private String clothesBottom;
    
    @Column(name = "clothes_etc", columnDefinition = "TEXT")
    private String clothesEtc;
    
    @Column(name = "missing_date", nullable = false)
    private LocalDateTime missingDate;
    
    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "photo_url")
    private String photoUrl;

    // GPS 위치 정보 (PostGIS Point 타입)
    // WGS84 좌표계(SRID: 4326) 사용
    // 형식: Point(경도, 위도)
    @Schema(hidden = true) // Swagger 문서에서 제외 (JTS Point 타입은 직렬화 불가)
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // 선택된 대표 이미지 URL (AI 생성 이미지 등)
    @Column(name = "predicted_face_url")
    private String predictedFaceUrl;

    @Column(name = "appearance_image_url")
    private String appearanceImageUrl;

    // 실종자의 현재 나이를 계산하여 반환합니다.
    //
    // @return 실종자의 현재 나이 (Integer)
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public Integer getMissingAge(){
        if (birthDate == null || missingDate == null) {
            return null;
        }
        return missingDate.getYear() - birthDate.getYear();
    }


    // 실종자의 신체 설명을 반환합니다.
    // body 필드의 별칭(alias) 메서드입니다.
    //
    // <p>AI 이미지 생성 시 프롬프트에 사용됩니다.</p>
    //
    // @return 실종자의 신체 설명 (얼굴 특징, 체형 등)
    public String getDescription() {
        return this.body;
    }

    // 실종자 정보 생성 (Factory Method)
    // DTO에서 직접 생성하여 서비스 레이어 의존성 제거
    public static MissingPerson from(
            String name,
            String birthDate,
            String gender,
            String missingDate,
            String body,
            String bodyEtc,
            String clothesTop,
            String clothesBottom,
            String clothesEtc,
            Integer height,
            Integer weight,
            Point location,
            String address) {

        try {

            return MissingPerson.builder()
                    .name(name)
                    .birthDate(LocalDate.parse(birthDate))
                    .gender(gender != null ? GenderType.valueOf(gender) : null)
                    .missingDate(LocalDateTime.parse(missingDate))
                    .body(body)
                    .bodyEtc(bodyEtc)
                    .clothesTop(clothesTop)
                    .clothesBottom(clothesBottom)
                    .clothesEtc(clothesEtc)
                    .height(height)
                    .weight(weight)
                    .location(location)
                    .address(address)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("실종자 정보 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 실종자 정보 업데이트
    // JPA Dirty Checking을 활용하여 변경 감지
    public void updateFrom(
            String name,
            String birthDate,
            String body,
            String bodyEtc,
            String clothesTop,
            String clothesBottom,
            String clothesEtc,
            Integer height,
            Integer weight,
            Point location,
            String address,
            String missingDate) {

        try {
            if (name != null) {
                this.name = name;
            }
            if (birthDate != null && !birthDate.isEmpty()) {
                this.birthDate = LocalDate.parse(birthDate);
            }
            if (body != null) {
                this.body = body;
            }
            if (bodyEtc != null) {
                this.bodyEtc = bodyEtc;
            }
            if (clothesTop != null) {
                this.clothesTop = clothesTop;
            }
            if (clothesBottom != null) {
                this.clothesBottom = clothesBottom;
            }
            if (clothesEtc != null) {
                this.clothesEtc = clothesEtc;
            }
            if (height != null) {
                this.height = height;
            }
            if (weight != null) {
                this.weight = weight;
            }
            if (location != null) {
                this.location = location;
            }
            if (address != null) {
                this.address = address;
            }
            if (missingDate != null && !missingDate.isEmpty()) {
                this.missingDate = LocalDateTime.parse(missingDate);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("실종자 정보 수정 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 위치 정보만 업데이트
    // JPA Dirty Checking 활용
    public void updateLocation(Point location, String address) {
        this.location = location;
        this.address = address;
    }

    // 위도 가져오기
    public Double getLatitude() {
        if (location == null)  throw new IllegalStateException("Location is not set");
        return location.getY();
    }

    // 경도 가져오기
    public Double getLongitude() {
        if (location == null)  throw new IllegalStateException("Location is not set");
        return location.getX();
    }


    public void updateAiImage(String assetUrl, AssetType assetType) {
        if (assetType == AssetType.AGE_PROGRESSION) {
            this.predictedFaceUrl = assetUrl;
        }
        else {
            this.appearanceImageUrl = assetUrl;
        }
    }
}