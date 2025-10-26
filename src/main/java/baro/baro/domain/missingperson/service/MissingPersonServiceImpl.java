package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
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
                .birthDate(request.getBirthDate() != null ? LocalDate.parse(request.getBirthDate()) : null)
                .body(request.getBody())
                .bodyEtc(request.getBodyEtc())
                .clothesTop(request.getClothesTop())
                .clothesBottom(request.getClothesBottom())
                .clothesEtc(request.getClothesEtc())
                .height(request.getHeight())
                .weight(request.getWeight())
                .location(request.getLocation())
                .address(address)
                .missingDate(request.getMissingDate() != null ? ZonedDateTime.parse(request.getMissingDate()) : null)
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
        String address = request.getLocation() != null ? getAddressFromLocation(request.getLocation()) : missingPerson.getAddress();

        // MissingPerson 업데이트
        missingPerson = MissingPerson.builder()
                .id(missingPerson.getId())
                .name(request.getName() != null ? request.getName() : missingPerson.getName())
                .birthDate(request.getBirthDate() != null ? LocalDate.parse(request.getBirthDate()) : missingPerson.getBirthDate())
                .gender(missingPerson.getGender())
                .body(request.getBody() != null ? request.getBody() : missingPerson.getBody())
                .bodyEtc(request.getBodyEtc() != null ? request.getBodyEtc() : missingPerson.getBodyEtc())
                .clothesTop(request.getClothesTop() != null ? request.getClothesTop() : missingPerson.getClothesTop())
                .clothesBottom(request.getClothesBottom() != null ? request.getClothesBottom() : missingPerson.getClothesBottom())
                .clothesEtc(request.getClothesEtc() != null ? request.getClothesEtc() : missingPerson.getClothesEtc())
                .height(request.getHeight() != null ? request.getHeight() : missingPerson.getHeight())
                .weight(request.getWeight() != null ? request.getWeight() : missingPerson.getWeight())
                .location(request.getLocation() != null ? request.getLocation() : missingPerson.getLocation())
                .address(address)
                .missingDate(request.getMissingDate() != null ? ZonedDateTime.parse(request.getMissingDate()) : missingPerson.getMissingDate())
                .createdAt(missingPerson.getCreatedAt())
                .updatedAt(missingPerson.getUpdatedAt())
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
        Page<MissingPerson> missingPersons = missingPersonRepository.findAllOpenCases(pageable);

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

    @Override
    @Transactional(readOnly = true)
    public MissingPersonDetailResponse getMissingPersonDetail(Long id) {
        MissingPerson missingPerson = missingPersonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("실종자를 찾을 수 없습니다."));

        return convertToDetailResponse(missingPerson);
    }

    private MissingPersonResponse convertToResponse(MissingPerson missingPerson) {
        return MissingPersonResponse.builder()
                .missingPersonId(missingPerson.getId())
                .name(missingPerson.getName())
                .address(missingPerson.getAddress())
                .missingDate(missingPerson.getMissingDate() != null ? missingPerson.getMissingDate().toString() : null)
                .height(missingPerson.getHeight())
                .weight(missingPerson.getWeight())
                .body(missingPerson.getBody())
                .build();
    }

    private MissingPersonDetailResponse convertToDetailResponse(MissingPerson missingPerson) {
        return MissingPersonDetailResponse.builder()
                .missingPersonId(missingPerson.getId())
                .name(missingPerson.getName())
                .birthDate(missingPerson.getBirthDate() != null ? missingPerson.getBirthDate().toString() : null)
                .address(missingPerson.getAddress())
                .missingDate(missingPerson.getMissingDate() != null ? missingPerson.getMissingDate().toString() : null)
                .height(missingPerson.getHeight())
                .weight(missingPerson.getWeight())
                .body(missingPerson.getBody())
                .bodyEtc(missingPerson.getBodyEtc())
                .clothesTop(missingPerson.getClothesTop())
                .clothesBottom(missingPerson.getClothesBottom())
                .clothesEtc(missingPerson.getClothesEtc())
                .location(missingPerson.getLocation())
                .photoUrl(null) // TODO: PersonMedia 엔티티와 연결하여 photo_url 가져오기
                .build();
    }

    private String getAddressFromLocation(String location) {
        // 실제 구현에서는 지오코딩 API를 사용하여 좌표를 주소로 변환
        // 여기서는 간단히 문자열로 반환
        return "주소 변환: " + location;
    }
}