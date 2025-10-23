package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissingPersonServiceImpl implements MissingPersonService {

    private final MissingPersonRepository missingPersonRepository;
    private final MissingCaseRepository missingCaseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request) {
        String currentUid = SecurityUtil.getCurrentUserUid();
        User reporter = userRepository.findByUid(currentUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 위치 정보를 주소로 변환
        String address = getAddressFromLocation(request.getLocation());

        // MissingPerson 엔티티 생성
        MissingPerson missingPerson = MissingPerson.builder()
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .description(request.getDescription())
                .location(request.getLocation())
                .address(address)
                .lastSeenDate(ZonedDateTime.parse(request.getLastSeenDate()))
                .build();

        missingPerson = missingPersonRepository.save(missingPerson);

        // MissingCase 엔티티 생성
        MissingCase missingCase = MissingCase.builder()
                .missingPerson(missingPerson)
                .reporter(reporter)
                .reportDate(ZonedDateTime.now())
                .status("ACTIVE")
                .build();

        missingCaseRepository.save(missingCase);

        return RegisterMissingPersonResponse.builder()
                .missingPersonId(missingPerson.getId())
                .build();
    }

    @Override
    @Transactional
    public RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("실종자를 찾을 수 없습니다."));

        // 위치 정보를 주소로 변환
        String address = getAddressFromLocation(request.getLocation());

        // MissingPerson 업데이트
        missingPerson = MissingPerson.builder()
                .id(missingPerson.getId())
                .name(request.getName() != null ? request.getName() : missingPerson.getName())
                .age(request.getAge() != null ? request.getAge() : missingPerson.getAge())
                .gender(request.getGender() != null ? request.getGender() : missingPerson.getGender())
                .description(request.getDescription() != null ? request.getDescription() : missingPerson.getDescription())
                .location(request.getLocation() != null ? request.getLocation() : missingPerson.getLocation())
                .address(request.getLocation() != null ? address : missingPerson.getAddress())
                .lastSeenDate(request.getLastSeenDate() != null ? ZonedDateTime.parse(request.getLastSeenDate()) : missingPerson.getLastSeenDate())
                .build();

        missingPerson = missingPersonRepository.save(missingPerson);

        return RegisterMissingPersonResponse.builder()
                .missingPersonId(missingPerson.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<MissingPerson> missingPersons = missingPersonRepository.findAll(pageable);

        return missingPersons.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request) {
        List<MissingPerson> nearbyPersons = missingPersonRepository.findNearbyMissingPersons(
                request.getLatitude(), 
                request.getLongitude(), 
                request.getRadius()
        );

        List<MissingPersonResponse> responses = nearbyPersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses);
    }

    private MissingPersonResponse convertToResponse(MissingPerson missingPerson) {
        return MissingPersonResponse.builder()
                .id(missingPerson.getId())
                .name(missingPerson.getName())
                .age(missingPerson.getAge())
                .gender(missingPerson.getGender())
                .description(missingPerson.getDescription())
                .location(missingPerson.getLocation())
                .address(missingPerson.getAddress())
                .lastSeenDate(missingPerson.getLastSeenDate())
                .build();
    }

    private String getAddressFromLocation(String location) {
        // 실제 구현에서는 지오코딩 API를 사용하여 좌표를 주소로 변환
        // 여기서는 간단히 문자열로 반환
        return "주소 변환: " + location;
    }
}