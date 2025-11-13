package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.FoundReportRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 실종자 관리 서비스 인터페이스
 */
public interface MissingPersonService {
    RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request);
    RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request);
    Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request);
    List<MissingPersonResponse> getMyMissingPersons();
    Page<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request);
    MissingPersonDetailResponse getMissingPersonDetail(Long id);
    void reportFound(FoundReportRequest request);

    /**
     * 실종자 발견을 신고합니다.
     *
     * @param request 발견 신고 요청 정보
     * @return 발견 신고 응답 정보
     */
    ReportSightingResponse reportSighting(ReportSightingRequest request);
}
