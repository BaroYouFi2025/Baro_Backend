package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MissingPersonService {
    RegisterMissingPersonResponse registerMissingPerson(RegisterMissingPersonRequest request);
    RegisterMissingPersonResponse updateMissingPerson(Long id, UpdateMissingPersonRequest request);
    Page<MissingPersonResponse> searchMissingPersons(SearchMissingPersonRequest request);
    List<MissingPersonResponse> findNearbyMissingPersons(NearbyMissingPersonRequest request);
}
