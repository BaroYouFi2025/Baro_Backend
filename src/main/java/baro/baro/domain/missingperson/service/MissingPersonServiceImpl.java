package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.ReportSightingRequest;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.entity.Sighting;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.missingperson.repository.SightingRepository;
import baro.baro.domain.common.util.LocationUtil;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.notification.dto.event.MissingPersonFoundNotificationEvent;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissingPersonServiceImpl implements MissingPersonService {

    private final MissingPersonRepository missingPersonRepository;
    private final MissingCaseRepository missingCaseRepository;
    private final SightingRepository sightingRepository;
    private final LocationService locationService;
    private final MetricsService metricsService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request) {
        User currentUser = getCurrentUser();

        // 비관적 락을 사용한 카운트 조회로 동시성 제어
        long registeredCount = missingCaseRepository.countByReportedByAndCaseStatusWithLock(currentUser, CaseStatusType.OPEN);
        if (registeredCount >= 4) {
            throw new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_LIMIT_EXCEEDED);
        }

        // 도메인 서비스: 위치 정보 생성
        LocationService.LocationInfo locationInfo = locationService.createLocationInfo(
                request.getLatitude(),
                request.getLongitude()
        );

        // 도메인: 실종자 엔티티 생성
        MissingPerson missingPerson = MissingPerson.from(
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                request.getMissingDate(),
                request.getBody(),
                request.getBodyEtc(),
                request.getClothesTop(),
                request.getClothesBottom(),
                request.getClothesEtc(),
                request.getHeight(),
                request.getWeight(),
                request.getPhotoUrl(),
                locationInfo.point(),
                locationInfo.address()
        );

        missingPerson = missingPersonRepository.save(missingPerson);

        // 도메인: 실종 케이스 생성
        MissingCase missingCase = MissingCase.reportBy(missingPerson, currentUser);
        missingCaseRepository.save(missingCase);

        log.info("실종자 등록 완료: id={}, name={}", missingPerson.getId(), missingPerson.getName());
        
        // 메트릭 기록: 실종자 신고
        metricsService.recordMissingPersonReport();
        
        return RegisterMissingPersonResponse.create(missingPerson.getId());
    }

    @Override
    @Transactional
    public RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 도메인 서비스: 위치 정보 변환
        String address = null;
        Point location = null;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            LocationService.LocationInfo locationInfo = locationService.createLocationInfo(
                    request.getLatitude(),
                    request.getLongitude()
            );
            address = locationInfo.address();
            location = locationInfo.point();
        }

        // 도메인: 실종자 정보 업데이트
        missingPerson.updateFrom(
                request.getName(),
                request.getBirthDate(),
                request.getBody(),
                request.getBodyEtc(),
                request.getClothesTop(),
                request.getClothesBottom(),
                request.getClothesEtc(),
                request.getHeight(),
                request.getWeight(),
                request.getPhotoUrl(),
                location,
                address,
                request.getMissingDate()
        );

        log.info("실종자 정보 수정 완료: id={}, name={}", id, missingPerson.getName());
        return RegisterMissingPersonResponse.create(missingPerson.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<MissingPerson> missingPersons = missingPersonRepository.findAllOpenCases(CaseStatusType.OPEN, pageable);

        log.debug("실종자 검색 완료: page={}, size={}, totalElements={}",
                request.getPage(), request.getSize(), missingPersons.getTotalElements());
        return missingPersons.map(MissingPersonResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissingPersonResponse> getMyMissingPersons() {
        User currentUser = getCurrentUser();
        List<MissingPerson> missingPersons = missingPersonRepository.findAllByReporterId(currentUser.getId(), CaseStatusType.OPEN);

        log.debug("내가 등록한 실종자 조회 완료: userId={}, count={}",
                currentUser.getId(), missingPersons.size());
        return missingPersons.stream()
                .map(MissingPersonResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request) {
        // 좌표 검증은 LocationUtil.createPoint에서 자동 수행
        LocationUtil.validateCoordinates(request.getLatitude(), request.getLongitude());

        List<MissingPerson> nearbyPersons = missingPersonRepository.findNearbyMissingPersons(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius()
        );

        List<MissingPersonResponse> responses = nearbyPersons.stream()
                .map(MissingPersonResponse::from)
                .collect(Collectors.toList());

        log.debug("주변 실종자 검색 완료: lat={}, lon={}, radius={}, count={}",
                request.getLatitude(), request.getLongitude(), request.getRadius(), responses.size());
        return new org.springframework.data.domain.PageImpl<>(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public MissingPersonDetailResponse getMissingPersonDetail(Long id) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        log.debug("실종자 상세 조회 완료: id={}, name={}", id, missingPerson.getName());
        return MissingPersonDetailResponse.from(missingPerson);
    }

    @Override
    @Transactional
    public ReportSightingResponse reportSighting(ReportSightingRequest request) {
        User currentUser = getCurrentUser();

        // 1. 실종자 조회
        MissingPerson missingPerson = missingPersonRepository.findById(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 2. 실종 케이스 조회 (OPEN 상태인 케이스만)
        MissingCase missingCase = missingCaseRepository.findByMissingPersonAndCaseStatus(
                missingPerson,
                CaseStatusType.OPEN
        ).orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.NO_ACTIVE_CASE_FOUND));

        // 3. 이미 종료된 케이스인지 확인
        if (missingCase.getCaseStatus() == CaseStatusType.CLOSED) {
            throw new MissingPersonException(MissingPersonErrorCode.CASE_ALREADY_CLOSED);
        }

        // 4. 중복 신고 체크 (최근 10분 이내 같은 실종자에 대한 신고 방지)
        // 비관적 락을 사용하여 동시성 제어
        java.time.ZonedDateTime tenMinutesAgo = java.time.ZonedDateTime.now().minusMinutes(10);
        boolean hasRecentReport = sightingRepository.existsRecentSightingWithLock(
                missingCase,
                currentUser,
                tenMinutesAgo
        );

        if (hasRecentReport) {
            log.warn("중복 신고 감지 - 사용자: {}, 실종자: {}, 케이스: {}",
                    currentUser.getName(), missingPerson.getName(), missingCase.getId());
            throw new MissingPersonException(MissingPersonErrorCode.DUPLICATE_SIGHTING_REPORT);
        }

        // 5. 위치 정보 변환
        LocationUtil.validateCoordinates(request.getLatitude(), request.getLongitude());
        LocationService.LocationInfo locationInfo = locationService.createLocationInfo(
                request.getLatitude(),
                request.getLongitude()
        );

        // 6. Sighting 엔티티 생성 및 저장
        Sighting sighting = Sighting.create(
                missingCase,
                currentUser,
                locationInfo.point(),
                locationInfo.address()
        );
        Sighting created = sightingRepository.save(sighting);

        // 7. 실종자 등록자에게 푸시 알림 발송 이벤트 게시
        User missingPersonOwner = missingCase.getReportedBy();
        try {
            eventPublisher.publishEvent(new MissingPersonFoundNotificationEvent(
                    this,
                    created.getId(),
                    missingPersonOwner,
                    missingPerson.getName(),
                    currentUser.getName(),
                    locationInfo.address()
            ));
        } catch (Exception e) {
            log.error("실종자 발견 알림 이벤트 발행 실패 - sightingId: {}", created.getId(), e);
        }

        log.info("실종자 발견 신고 완료 - 실종자: {}, 신고자: {}, 등록자: {}, 위치: {}",
                missingPerson.getName(), currentUser.getName(), missingPersonOwner.getName(), locationInfo.address());

        // 메트릭 기록: 실종자 발견
        metricsService.recordMissingPersonFound();

        return ReportSightingResponse.success();
    }


    @Override
    @Transactional
    public void closeMissingCase(Long missingPersonId) {
        User currentUser = getCurrentUser();

        // 1. 실종자 조회
        MissingPerson missingPerson = missingPersonRepository.findById(missingPersonId)
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 2. 활성 케이스 조회
        MissingCase missingCase = missingCaseRepository.findByMissingPersonAndCaseStatus(
                missingPerson,
                CaseStatusType.OPEN
        ).orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.NO_ACTIVE_CASE_FOUND));

        // 3. 도메인 로직 실행 (권한 검증 + 케이스 종료)
        missingCase.closeBy(currentUser);

        log.info("실종 케이스 종료 완료 - 실종자: {}, 종료자: {}", missingPerson.getName(), currentUser.getName());
    }
}
