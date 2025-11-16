package baro.baro.domain.missingperson.entity;

import baro.baro.domain.missingperson.dto.external.PoliceApiMissingPerson;
import baro.baro.domain.missingperson.dto.res.MissingPersonPoliceResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

// 경찰청 실종자 도메인 엔티티
// DDD 원칙: 비즈니스 로직을 도메인 내부에 캡슐화
@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "missing_person_police", schema = "youfi")
public class MissingPersonPolice {

    @Id
    @Column(name = "id")
    private Long id; // msspsnIdntfccd 값이 들어감

    @Column(name = "occurrence_date", length = 8)
    private String occurrenceDate; // 발생일 (YYYYMMDD)

    @Column(name = "dress", columnDefinition = "TEXT")
    private String dress; // 착의사항

    @Column(name = "age_now")
    private Integer ageNow; // 현재나이

    @Column(name = "missing_age")
    private Integer missingAge; // 실종 당시 나이

    @Column(name = "status_code", length = 10)
    private String statusCode; // 실종자 상태 코드

    @Column(name = "gender", length = 10)
    private String gender; // 성별

    @Column(name = "special_features", columnDefinition = "TEXT")
    private String specialFeatures; // 기타 특징

    @Column(name = "occurrence_address", columnDefinition = "TEXT")
    private String occurrenceAddress; // 발생 장소

    @Column(name = "name", length = 100)
    private String name; // 이름

    @Column(name = "photo_length")
    private Integer photoLength; // Base64 인코딩된 이미지 길이

    @Transient
    private String photoBase64Temp; // API에서 받은 Base64 데이터 임시 저장 (DB 저장 안 함)

    @Column(name = "photo_url", length = 500)
    private String photoUrl; // 저장된 얼굴 사진 URL

    @Column(name = "collected_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectedAt; // 데이터 수집 시각

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // 생성일

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // 수정일

    // ============ 정적 팩토리 메서드 (생성) ============

    // 경찰청 API 응답으로부터 새로운 엔티티 생성
    public static MissingPersonPolice createFromPoliceApi(PoliceApiMissingPerson apiData) {
        MissingPersonPolice entity = new MissingPersonPolice();
        entity.id = apiData.getMsspsnIdntfccd();
        entity.occurrenceDate = apiData.getOccrde();
        entity.dress = apiData.getAlldressingDscd();
        entity.ageNow = parseAgeNow(apiData.getAgeNow(), apiData.getMsspsnIdntfccd());
        entity.missingAge = apiData.getAge();
        entity.statusCode = apiData.getWritngTrgetDscd();
        entity.gender = apiData.getSexdstnDscd();
        entity.specialFeatures = apiData.getEtcSpfeatr();
        entity.occurrenceAddress = apiData.getOccrAdres();
        entity.name = apiData.getNm();
        entity.photoLength = apiData.getTknphotolength();
        entity.photoBase64Temp = apiData.getTknphotoFile(); // Base64 데이터 임시 저장 (DB 저장 안 함)
        entity.collectedAt = new Date();
        return entity;
    }

    // ============ 비즈니스 로직 (업데이트) ============

    // API 데이터로 엔티티 업데이트
    public void updateFromPoliceApi(PoliceApiMissingPerson apiData) {
        this.occurrenceDate = apiData.getOccrde();
        this.dress = apiData.getAlldressingDscd();
        this.ageNow = parseAgeNow(apiData.getAgeNow(), apiData.getMsspsnIdntfccd());
        this.missingAge = apiData.getAge();
        this.statusCode = apiData.getWritngTrgetDscd();
        this.gender = apiData.getSexdstnDscd();
        this.specialFeatures = apiData.getEtcSpfeatr();
        this.occurrenceAddress = apiData.getOccrAdres();
        this.name = apiData.getNm();
        this.photoLength = apiData.getTknphotolength();
        this.photoBase64Temp = apiData.getTknphotoFile(); // Base64 데이터 임시 저장 (DB 저장 안 함)
        this.collectedAt = new Date();
    }

    public MissingPersonPoliceResponse toDto(){
        MissingPersonPoliceResponse response = new MissingPersonPoliceResponse();
        response.setMissingPersonPoliceId(this.getId());
        response.setName(this.name);
        response.setDress(this.dress);
        response.setAddress(this.occurrenceAddress);
        return response;
    }

    // ============ 도메인 메서드 (사진 처리) ============

    // 사진 URL 설정 (이벤트 리스너에서 호출)
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // Base64 임시 데이터 존재 여부 확인
    public boolean hasPhotoBase64Temp() {
        return this.photoBase64Temp != null && !this.photoBase64Temp.isBlank();
    }

    // Base64 임시 데이터 초기화 (처리 완료 후 메모리 해제)
    public void clearPhotoBase64Temp() {
        this.photoBase64Temp = null;
    }

    // ============ 내부 헬퍼 메서드 ============

    private static Integer parseAgeNow(String ageNowString, Long id) {
        if (ageNowString == null || ageNowString.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(ageNowString);
        } catch (NumberFormatException e) {
            log.warn("현재나이 변환 실패 (ID: {}): {}", id, ageNowString);
            return null;
        }
    }

    // ============ JPA 생명주기 콜백 ============

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        if (this.collectedAt == null) {
            this.collectedAt = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
