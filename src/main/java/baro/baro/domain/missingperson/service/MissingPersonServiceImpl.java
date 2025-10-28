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
import baro.baro.domain.missingperson.entity.GenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                .birthDate(request.getAge() != null ? LocalDate.now().minusYears(request.getAge()) : null)
                .gender(GenderType.valueOf(request.getGender()))
                .body(request.getDescription())
                .location(request.getLocation())
                .address(address)
                .missingDate(ZonedDateTime.parse(request.getLastSeenDate()))
                .build();

        missingPerson = missingPersonRepository.save(missingPerson);

        // MissingCase 엔티티 생성
        MissingCase missingCase = MissingCase.builder()
                .missingPerson(missingPerson)
                .reportedBy(reporter)
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
                .birthDate(request.getAge() != null ? LocalDate.now().minusYears(request.getAge()) : missingPerson.getBirthDate())
                .gender(request.getGender() != null ? GenderType.valueOf(request.getGender()) : missingPerson.getGender())
                .body(request.getDescription() != null ? request.getDescription() : missingPerson.getBody())
                .location(request.getLocation() != null ? request.getLocation() : missingPerson.getLocation())
                .address(request.getLocation() != null ? address : missingPerson.getAddress())
                .missingDate(request.getLastSeenDate() != null ? ZonedDateTime.parse(request.getLastSeenDate()) : missingPerson.getMissingDate())
                .predictedFaceUrl(null)
                .appearanceImageUrl(null)
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
                .gender(missingPerson.getGender() != null ? missingPerson.getGender().toString() : null)
                .description(missingPerson.getDescription())
                .location(missingPerson.getLocation())
                .address(missingPerson.getAddress())
                .lastSeenDate(missingPerson.getLastSeenDate())
                .selectedImageUrl(missingPerson.getSelectedImageUrl())
                .build();
    }

    private String getAddressFromLocation(String location) {
        // 실제 구현에서는 지오코딩 API를 사용하여 좌표를 주소로 변환
        // 여기서는 간단히 문자열로 반환
        return "주소 변환: " + location;
    }
}
