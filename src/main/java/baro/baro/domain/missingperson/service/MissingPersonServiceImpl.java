package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.FoundReportRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.Sighting;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.missingperson.repository.SightingRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.util.LocationUtil;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.notification.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request) {
        User currentUser = getCurrentUser();

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
                locationInfo.point(),
                locationInfo.address()
        );

        missingPerson = missingPersonRepository.save(missingPerson);

        // 도메인: 실종 케이스 생성
        MissingCase missingCase = MissingCase.reportBy(missingPerson, currentUser);
        missingCaseRepository.save(missingCase);

        log.info("실종자 등록 완료: id={}, name={}", missingPerson.getId(), missingPerson.getName());
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
        Page<MissingPerson> missingPersons = missingPersonRepository.findAllOpenCases(pageable);

        log.debug("실종자 검색 완료: page={}, size={}, totalElements={}",
                request.getPage(), request.getSize(), missingPersons.getTotalElements());
        return missingPersons.map(MissingPersonResponse::from);
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
    public void reportFound(FoundReportRequest request) {
        // 1. 실종자 정보 조회
        MissingPerson missingPerson = missingPersonRepository.findById(request.getMissingPersonId())
                .orElseThrow(() -> new MissingPersonException(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND));

        // 2. 실종 케이스 조회
        MissingCase missingCase = missingCaseRepository.findByMissingPerson(missingPerson)
                .orElseThrow(() -> new IllegalArgumentException("실종 케이스를 찾을 수 없습니다."));

        // 3. 현재 사용자 조회 (발견 신고자)
        String currentUid = SecurityUtil.getCurrentUserUid();
        User reporter = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 위치 Point 객체 생성
        Point locationPoint = LocationUtil.createPoint(
                request.getLatitude(),
                request.getLongitude()
        );

        // 5. 목격 신고 저장
        Sighting sighting = Sighting.builder()
                .missingCase(missingCase)
                .reporter(reporter)
                .location(locationPoint)
                .build();
        sightingRepository.save(sighting);

        // 6. 실종자 등록자에게 푸시 알림 발송
        User originalReporter = missingCase.getReportedBy();
        String locationText = request.getLocation() != null 
                ? request.getLocation() 
                : String.format("위도: %.6f, 경도: %.6f", request.getLatitude(), request.getLongitude());
        pushNotificationService.sendFoundNotification(
                originalReporter,
                missingPerson.getName(),
                locationText
        );

        log.info("실종자 발견 신고 완료: id={}, name={}, reportedBy={}", 
                missingPerson.getId(), missingPerson.getName(), reporter.getName());
    }
}