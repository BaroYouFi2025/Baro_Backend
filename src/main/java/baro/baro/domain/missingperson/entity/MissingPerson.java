package baro.baro.domain.missingperson.entity;

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
    
    /**
     * GPS 위치 정보 (PostGIS Point 타입)
     * WGS84 좌표계(SRID: 4326) 사용
     * 형식: Point(경도, 위도)
     */
    @Schema(hidden = true) // Swagger 문서에서 제외 (JTS Point 타입은 직렬화 불가)
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * 실종자의 나이를 계산하여 반환합니다.
     *
     * @return 실종자의 나이 (Integer)
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    
    /**
     * 실종자 정보 생성 (Factory Method)
     * 빌더 패턴을 사용하여 불변성 보장
     */
    public static MissingPerson create(
            String name,
            LocalDate birthDate,
            GenderType gender,
            String body,
            String bodyEtc,
            String clothesTop,
            String clothesBottom,
            String clothesEtc,
            Integer height,
            Integer weight,
            Point location,
            String address,
            ZonedDateTime missingDate) {
        
        return MissingPerson.builder()
                .name(name)
                .birthDate(birthDate)
                .gender(gender)
                .body(body)
                .bodyEtc(bodyEtc)
                .clothesTop(clothesTop)
                .clothesBottom(clothesBottom)
                .clothesEtc(clothesEtc)
                .height(height)
                .weight(weight)
                .location(location)
                .address(address)
                .missingDate(missingDate)
                .build();
    }

    /**
     * 실종자 정보 업데이트
     * JPA Dirty Checking을 활용하여 변경 감지
     * 도메인 로직을 캡슐화
     */
    public void update(
            String name,
            LocalDate birthDate,
            String body,
            String bodyEtc,
            String clothesTop,
            String clothesBottom,
            String clothesEtc,
            Integer height,
            Integer weight,
            Point location,
            String address,
            ZonedDateTime missingDate) {

        if (name != null) {
            this.name = name;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
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
        if (missingDate != null) {
            this.missingDate = missingDate;
        }
    }

    /**
     * 위치 정보만 업데이트
     * JPA Dirty Checking 활용
     */
    public void updateLocation(Point location, String address) {
        this.location = location;
        this.address = address;
    }
    
    /**
     * 위도 가져오기
     */
    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }
    
    /**
     * 경도 가져오기
     */
    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }
    
    /**
     * 좌표 문자열 생성 ("위도,경도" 형식)
     */
    public String getCoordinatesString() {
        if (location == null) {
            return null;
        }
        return location.getY() + "," + location.getX();
    }
}