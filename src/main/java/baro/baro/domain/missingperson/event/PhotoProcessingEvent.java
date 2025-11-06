package baro.baro.domain.missingperson.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 경찰청 실종자 사진 처리 이벤트
 * 엔티티 저장 후 별도 트랜잭션에서 사진 파일 처리를 위한 이벤트
 */
@Getter
public class PhotoProcessingEvent extends ApplicationEvent {

    private final Long missingPersonId;
    private final String photoBase64Data;
    private final boolean preserveExistingOnFailure;

    /**
     * @param source 이벤트를 발행한 객체 (일반적으로 Service 인스턴스)
     * @param missingPersonId 실종자 ID
     * @param photoBase64Data Base64 인코딩된 사진 데이터
     * @param preserveExistingOnFailure 실패 시 기존 URL 유지 여부
     */
    public PhotoProcessingEvent(Object source, Long missingPersonId, String photoBase64Data, boolean preserveExistingOnFailure) {
        super(source);
        this.missingPersonId = missingPersonId;
        this.photoBase64Data = photoBase64Data;
        this.preserveExistingOnFailure = preserveExistingOnFailure;
    }
}
