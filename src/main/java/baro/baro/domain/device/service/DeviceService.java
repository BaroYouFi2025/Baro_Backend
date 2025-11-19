package baro.baro.domain.device.service;

import baro.baro.domain.device.dto.request.DeviceRegisterRequest;
import baro.baro.domain.device.dto.request.FcmTokenUpdateRequest;
import baro.baro.domain.device.dto.request.GpsUpdateRequest;
import baro.baro.domain.device.dto.response.DeviceResponse;
import baro.baro.domain.device.dto.response.GpsUpdateResponse;

public interface DeviceService {
    DeviceResponse registerDevice(DeviceRegisterRequest request);
    GpsUpdateResponse updateGps(GpsUpdateRequest request);
    void updateFcmToken(String uid, FcmTokenUpdateRequest request);
}
