package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import org.springframework.data.domain.Page;

public interface MissingPersonService {
    RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request);
    RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request);
    Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request);
    Page<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request);
    MissingPersonDetailResponse getMissingPersonDetail(Long id);
}
