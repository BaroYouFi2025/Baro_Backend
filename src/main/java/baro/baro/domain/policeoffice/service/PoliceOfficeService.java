package baro.baro.domain.policeoffice.service;

import baro.baro.domain.policeoffice.dto.req.NearbyPoliceOfficeRequest;
import baro.baro.domain.policeoffice.dto.res.PoliceOfficeResponse;

import java.util.List;

public interface PoliceOfficeService {
    List<PoliceOfficeResponse> findNearbyOffices(NearbyPoliceOfficeRequest request);
}
