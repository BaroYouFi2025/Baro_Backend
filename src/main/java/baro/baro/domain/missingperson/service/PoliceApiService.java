package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.external.PoliceApiMissingPerson;
import baro.baro.domain.missingperson.dto.external.PoliceApiResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonPoliceResponse;
import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import baro.baro.domain.missingperson.event.PhotoProcessingEvent;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingPersonPoliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoliceApiService {

    @Qualifier("policeApiWebClient")
    private final WebClient policeApiWebClient;

    private final MissingPersonPoliceRepository policeRepository;

    private final JdbcTemplate jdbcTemplate;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${police.api.esntl-id}")
    private String esntlId;

    @Value("${police.api.auth-key}")
    private String authKey;

    @Value("${police.api.row-size}")
    private int rowSize;

    // 메모리 최적화: 청크 단위 처리 사이즈
    private static final int CHUNK_SIZE = 500;

    // 경찰청 API로부터 실종자 데이터를 가져와 저장 (메모리 최적화 버전)
    // 청크 단위로 처리하여 대용량 데이터도 안정적으로 처리
    @Transactional
    public void syncMissingPersonsFromPoliceApi() {
        log.info("경찰청 실종자 데이터 동기화 시작 (메모리 최적화 모드)");

        try {
            // 1. 첫 페이지로 전체 데이터 크기 파악
            PoliceApiResponse firstPage = fetchFromPoliceApi(1);

            // 2. 응답 검증
            if (firstPage == null || firstPage.getList() == null) {
                log.warn("경찰청 API 응답이 비어있습니다.");
                return;
            }

            int declaredTotalCount = firstPage.getTotalCount() != null ? firstPage.getTotalCount() : firstPage.getList().size();
            int pageSize = rowSize > 0 ? rowSize : firstPage.getList().size();
            if (pageSize <= 0) {
                pageSize = firstPage.getList().size();
            }

            int totalPages = (int) Math.ceil((double) declaredTotalCount / pageSize);
            log.info("총 {}건의 데이터를 {}페이지에서 수집 예정", declaredTotalCount, totalPages);

            // 3. 청크 단위로 스트림 처리
            SyncResult totalResult = new SyncResult();
            List<PoliceApiMissingPerson> chunk = new ArrayList<>(CHUNK_SIZE);

            // 첫 페이지 데이터 추가
            chunk.addAll(firstPage.getList());

            // 나머지 페이지 처리
            for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
                PoliceApiResponse nextPage = fetchFromPoliceApi(pageNo);
                if (nextPage == null || nextPage.getList() == null || nextPage.getList().isEmpty()) {
                    log.warn("경찰청 API {} 페이지 응답이 비어있습니다.", pageNo);
                    break;
                }

                chunk.addAll(nextPage.getList());

                // 청크 사이즈 도달 시 처리
                if (chunk.size() >= CHUNK_SIZE) {
                    SyncResult chunkResult = processChunk(chunk);
                    totalResult.insertCount += chunkResult.insertCount;
                    totalResult.updateCount += chunkResult.updateCount;
                    log.info("청크 처리 완료 ({}건) - 누적: 신규 {}, 업데이트 {}",
                            chunk.size(), totalResult.insertCount, totalResult.updateCount);
                    chunk.clear(); // 메모리 해제
                }
            }

            // 마지막 남은 청크 처리
            if (!chunk.isEmpty()) {
                SyncResult chunkResult = processChunk(chunk);
                totalResult.insertCount += chunkResult.insertCount;
                totalResult.updateCount += chunkResult.updateCount;
                log.info("마지막 청크 처리 완료 ({}건)", chunk.size());
            }

            // 5. 최종 결과 로깅
            log.info("경찰청 실종자 데이터 동기화 완료 - 총 신규: {}, 총 업데이트: {}",
                    totalResult.insertCount, totalResult.updateCount);

        } catch (WebClientResponseException e) {
            log.error("경찰청 API 응답 오류 (HTTP {}): {}", e.getStatusCode(), e.getMessage());
            log.debug("Full response body:", e.getResponseBodyAsString());
            throw new MissingPersonException(MissingPersonErrorCode.POLICE_API_RESPONSE_ERROR);

        } catch (WebClientRequestException e) {
            log.error("경찰청 API 네트워크 오류: {}", e.getMessage());
            log.debug("Full stack trace:", e);
            throw new MissingPersonException(MissingPersonErrorCode.POLICE_API_NETWORK_ERROR);

        } catch (DataIntegrityViolationException e) {
            log.error("데이터베이스 무결성 오류: {}", e.getMessage());
            log.debug("Full stack trace:", e);
            throw new MissingPersonException(MissingPersonErrorCode.POLICE_API_DATA_SAVE_FAILED);

        } catch (Exception e) {
            log.error("경찰청 실종자 데이터 동기화 중 예기치 않은 오류 발생: {}", e.getMessage());
            log.debug("Full stack trace:", e);
            throw new MissingPersonException(MissingPersonErrorCode.POLICE_API_CALL_FAILED);
        }
    }

    // 청크 단위 데이터 처리 (메모리 최적화)
    private SyncResult processChunk(List<PoliceApiMissingPerson> chunk) {
        // 엔티티 변환
        List<MissingPersonPolice> entities = convertToEntities(chunk);

        // Batch Upsert 실행
        return performBatchUpsert(entities, chunk);
    }

    // 경찰청 API 호출
    private PoliceApiResponse fetchFromPoliceApi(int page) {
        return policeApiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/lcm/findChildList.do")
                        .queryParam("esntlId", esntlId)
                        .queryParam("authKey", authKey)
                        .queryParam("rowSize", rowSize)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(PoliceApiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    // API 응답 DTO를 엔티티로 변환 (도메인 정적 팩토리 메서드 사용)
    private List<MissingPersonPolice> convertToEntities(List<PoliceApiMissingPerson> apiList) {
        return apiList.stream()
                .map(MissingPersonPolice::createFromPoliceApi)
                .collect(Collectors.toList());
    }

    // Batch Upsert 실행 (JDBC Batch + 이벤트 발행)
    // 1. JDBC Batch UPSERT (100개씩 묶어서 실행)
    // 2. 사진 처리 이벤트 발행 (비동기 처리)
    private SyncResult performBatchUpsert(List<MissingPersonPolice> entities,
                                           List<PoliceApiMissingPerson> apiResults) {
        SyncResult result = new SyncResult();
        Date now = new Date();

        // 1. 기존 ID 조회 (카운팅용)
        Set<Long> existingIds = policeRepository.findAllIds();

        // 결과 집계
        for (MissingPersonPolice entity : entities) {
            if (existingIds.contains(entity.getId())) {
                result.updateCount++;
            } else {
                result.insertCount++;
            }
        }

        // 2. JDBC Batch UPSERT 실행 (사진 URL은 나중에 이벤트 리스너가 업데이트)
        String sql = """
            INSERT INTO youfi.missing_person_police
            (id, occurrence_date, dress, age_now, missing_age, status_code, gender,
             special_features, occurrence_address, name, photo_length, photo_url,
             collected_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id)
            DO UPDATE SET
                occurrence_date = EXCLUDED.occurrence_date,
                dress = EXCLUDED.dress,
                age_now = EXCLUDED.age_now,
                missing_age = EXCLUDED.missing_age,
                status_code = EXCLUDED.status_code,
                gender = EXCLUDED.gender,
                special_features = EXCLUDED.special_features,
                occurrence_address = EXCLUDED.occurrence_address,
                name = EXCLUDED.name,
                photo_length = EXCLUDED.photo_length,
                collected_at = EXCLUDED.collected_at,
                updated_at = EXCLUDED.updated_at
            """;

        jdbcTemplate.batchUpdate(sql, entities, 100, (ps, entity) -> {
            ps.setLong(1, entity.getId());
            ps.setString(2, entity.getOccurrenceDate());
            ps.setString(3, entity.getDress());
            ps.setObject(4, entity.getAgeNow());
            ps.setObject(5, entity.getMissingAge());
            ps.setString(6, entity.getStatusCode());
            ps.setString(7, entity.getGender());
            ps.setString(8, entity.getSpecialFeatures());
            ps.setString(9, entity.getOccurrenceAddress());
            ps.setString(10, entity.getName());
            ps.setObject(11, entity.getPhotoLength());
            ps.setString(12, null); // photo_url은 이벤트 리스너에서 업데이트
            ps.setTimestamp(13, new Timestamp(entity.getCollectedAt().getTime()));
            ps.setTimestamp(14, new Timestamp(now.getTime())); // INSERT용 created_at (UPDATE 시에는 미사용)
            ps.setTimestamp(15, new Timestamp(now.getTime())); // updated_at는 항상 갱신
        });

        log.info("경찰청 Batch UPSERT 완료 - 신규: {}, 업데이트: {}", result.insertCount, result.updateCount);

        // 3. 사진 처리 이벤트 발행 (비동기 처리)
        for (MissingPersonPolice entity : entities) {
            if (entity.hasPhotoBase64Temp()) {
                boolean preserveExisting = existingIds.contains(entity.getId());
                eventPublisher.publishEvent(new PhotoProcessingEvent(
                        this,
                        entity.getId(),
                        entity.getPhotoBase64Temp(),
                        preserveExisting
                ));
            }
        }

        log.info("사진 처리 이벤트 발행 완료 ({}건)", entities.size());
        return result;
    }

    // 전체 실종자 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<MissingPersonPoliceResponse> getAllMissingPersons(Pageable pageable) {
        return policeRepository.findAll(pageable)
                .map(MissingPersonPolice::toDto);
    }

    // 실종자 개별 조회
    @Transactional(readOnly = true)
    public MissingPersonPolice getMissingPersonById(Long id) {
        return policeRepository.findById(id)
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));
    }

    // 동기화 결과를 담는 내부 클래스
    private static class SyncResult {
        int insertCount = 0;
        int updateCount = 0;
    }
}
