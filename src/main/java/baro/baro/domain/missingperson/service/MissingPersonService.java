package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.ReportSightingRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import org.springframework.data.domain.Page;

/**
 * 실종자 관리 서비스 인터페이스
 */
public interface MissingPersonService {
    
    /**
     * 실종자를 등록합니다.
     */
    RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request);
    
    /**
     * 실종자 정보를 수정합니다.
     */
    RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request);
    
    /**
     * 실종자 목록을 검색합니다.
     */
    Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request);
    
    /**
     * 주변 실종자를 검색합니다.
     */
    Page<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request);
    
    /**
     * 실종자 상세 정보를 조회합니다.
     */
    MissingPersonDetailResponse getMissingPersonDetail(Long id);
    
    /**
     * 실종자 발견을 신고합니다.
     * 
     * @param request 발견 신고 요청 정보
     * @return 발견 신고 응답 정보
     */
    ReportSightingResponse reportSighting(ReportSightingRequest request);
}
