package baro.baro.domain.device.service;

import baro.baro.domain.device.dto.request.DeviceRegisterRequest;
import baro.baro.domain.device.dto.request.GpsUpdateRequest;
import baro.baro.domain.device.dto.response.DeviceLocationResponse;
import baro.baro.domain.device.dto.response.DeviceResponse;
import baro.baro.domain.device.dto.response.GpsUpdateResponse;

import java.util.List;

public interface DeviceService {
    DeviceResponse registerDevice(String uid, DeviceRegisterRequest request);
    GpsUpdateResponse updateGps(String uid, Long deviceId, GpsUpdateRequest request);
    
    /**
     * 사용자의 최신 기기 위치를 조회합니다.
     * 관계가 없는 사용자도 조회할 수 있습니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 최신 기기 위치 정보
     */
    DeviceLocationResponse getDeviceLocation(Long userId);
}
