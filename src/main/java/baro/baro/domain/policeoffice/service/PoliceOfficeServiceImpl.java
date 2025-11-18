package baro.baro.domain.policeoffice.service;

import baro.baro.domain.common.util.GpsUtils;
import baro.baro.domain.common.util.LocationUtil;
import baro.baro.domain.policeoffice.dto.req.NearbyPoliceOfficeRequest;
import baro.baro.domain.policeoffice.dto.res.PoliceOfficeResponse;
import baro.baro.domain.policeoffice.entity.PoliceOffice;
import baro.baro.domain.policeoffice.repository.PoliceOfficeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoliceOfficeServiceImpl implements PoliceOfficeService {

    private final PoliceOfficeRepository policeOfficeRepository;

    @Override
    public List<PoliceOfficeResponse> findNearbyOffices(NearbyPoliceOfficeRequest request) {
        LocationUtil.validateCoordinates(request.getLatitude(), request.getLongitude());

        List<PoliceOffice> offices = policeOfficeRepository.findNearbyOffices(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters(),
                request.getLimit()
        );

        List<PoliceOfficeResponse> responses = offices.stream()
                .map(office -> PoliceOfficeResponse.from(office, calculateDistance(request, office)))
                .collect(Collectors.toList());

        log.debug("근처 경찰관서 검색: lat={}, lon={}, radius={}, limit={}, result={}",
                request.getLatitude(), request.getLongitude(),
                request.getRadiusMeters(), request.getLimit(),
                responses.size());

        return responses;
    }

    private Double calculateDistance(NearbyPoliceOfficeRequest request, PoliceOffice office) {
        Point location = office.getLocation();
        if (location == null) {
            return null;
        }
        return GpsUtils.calculateDistance(
                request.getLatitude(),
                request.getLongitude(),
                location.getY(),
                location.getX()
        );
    }
}
