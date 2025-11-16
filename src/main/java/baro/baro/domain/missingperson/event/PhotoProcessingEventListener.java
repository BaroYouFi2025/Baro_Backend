package baro.baro.domain.missingperson.event;

import baro.baro.domain.image.service.ImageService;
import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import baro.baro.domain.missingperson.repository.MissingPersonPoliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

// 경찰청 실종자 사진 처리 이벤트 리스너
// 별도 트랜잭션에서 사진 파일 저장 및 URL 업데이트 수행
@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoProcessingEventListener {

    private final MissingPersonPoliceRepository policeRepository;
    private final ImageService imageService;

    // 사진 처리 이벤트 핸들러
    // - 비동기 실행으로 메인 트랜잭션 블로킹 방지
    // - 새로운 트랜잭션으로 파일 시스템 작업과 DB 업데이트 분리
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePhotoProcessing(PhotoProcessingEvent event) {
        Long id = event.getMissingPersonId();
        String base64Data = event.getPhotoBase64Data();
        boolean preserveExisting = event.isPreserveExistingOnFailure();

        log.info("사진 처리 이벤트 수신 (ID: {})", id);

        // 엔티티 조회
        Optional<MissingPersonPolice> optionalEntity = policeRepository.findById(id);
        if (optionalEntity.isEmpty()) {
            log.warn("사진 처리 실패: 엔티티 없음 (ID: {})", id);
            return;
        }

        MissingPersonPolice entity = optionalEntity.get();
        String currentUrl = entity.getPhotoUrl();

        // 빈 데이터 처리
        if (base64Data == null || base64Data.isBlank()) {
            if (!preserveExisting) {
                entity.setPhotoUrl(null);
                policeRepository.save(entity);
                log.info("빈 사진 데이터로 URL 초기화 (ID: {})", id);
            }
            return;
        }

        // 이미 URL인 경우 그대로 유지
        if (isUrl(base64Data)) {
            log.info("이미 URL 형식으로 저장됨, 스킵 (ID: {})", id);
            return;
        }

        // Base64 데이터 파싱
        ParsedPhoto parsed = parseBase64(base64Data);
        if (parsed == null) {
            if (!preserveExisting) {
                entity.setPhotoUrl(null);
                policeRepository.save(entity);
            }
            log.warn("Base64 파싱 실패 (ID: {})", id);
            return;
        }

        // 중복 저장 방지 (해시 비교)
        if (currentUrl != null && currentUrl.contains(parsed.hash())) {
            log.info("중복 사진 감지, 저장 스킵 (ID: {}, Hash: {})", id, parsed.hash());
            return;
        }

        // Base64 디코딩 및 파일 저장
        try {
            byte[] imageBytes = Base64.getDecoder().decode(parsed.payload());
            String filename = String.format("police_%d_%s.%s", id, parsed.hash(), parsed.extension());
            String photoUrl = imageService.saveImageFromBytes(imageBytes, filename, parsed.mimeType());

            entity.setPhotoUrl(photoUrl);
            policeRepository.save(entity);

            log.info("사진 저장 성공 (ID: {}, URL: {})", id, photoUrl);
        } catch (IllegalArgumentException e) {
            log.warn("실종자 사진 Base64 디코딩 실패 (ID: {}): {}", id, e.getMessage());
            if (!preserveExisting) {
                entity.setPhotoUrl(null);
                policeRepository.save(entity);
            }
        } catch (Exception e) {
            log.error("실종자 사진 저장 실패 (ID: {}): {}", id, e.getMessage());
            log.debug("사진 저장 실패 상세", e);
            if (!preserveExisting) {
                entity.setPhotoUrl(null);
                policeRepository.save(entity);
            }
        }
    }

    // ============ 내부 헬퍼 메서드 ============

    private boolean isUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private ParsedPhoto parseBase64(String raw) {
        try {
            String trimmed = raw.trim();
            String mimeType = "image/jpeg";
            String payload = trimmed;

            int commaIndex = trimmed.indexOf(',');
            if (trimmed.startsWith("data:") && commaIndex > 0) {
                String metadata = trimmed.substring(5, commaIndex);
                String[] parts = metadata.split(";");
                for (String part : parts) {
                    if (part.startsWith("image/")) {
                        mimeType = part;
                        break;
                    }
                }
                payload = trimmed.substring(commaIndex + 1);
            }

            String normalizedPayload = payload.replaceAll("\\s", "");
            String hash = DigestUtils.md5DigestAsHex(normalizedPayload.getBytes(StandardCharsets.UTF_8));
            String extension = resolveExtension(mimeType);

            return new ParsedPhoto(normalizedPayload, mimeType, extension, hash);
        } catch (Exception e) {
            log.warn("실종자 사진 메타데이터 파싱 실패: {}", e.getMessage());
            log.debug("사진 메타데이터 파싱 실패 상세", e);
            return null;
        }
    }

    private String resolveExtension(String mimeType) {
        if (mimeType == null) {
            return "jpg";
        }
        String lower = mimeType.toLowerCase();
        if (lower.contains("png")) return "png";
        if (lower.contains("gif")) return "gif";
        if (lower.contains("webp")) return "webp";
        return "jpg";
    }

    // ============ Value Object (내부 사용) ============

    private record ParsedPhoto(String payload, String mimeType, String extension, String hash) {}
}
