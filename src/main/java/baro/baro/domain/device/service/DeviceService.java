package baro.baro.domain.device.service;

import baro.baro.domain.device.dto.req.DeviceRegisterRequest;
import baro.baro.domain.device.dto.req.FcmTokenUpdateRequest;
import baro.baro.domain.device.dto.req.GpsUpdateRequest;
import baro.baro.domain.device.dto.res.DeviceResponse;
import baro.baro.domain.device.dto.res.GpsUpdateResponse;

public interface DeviceService {
    DeviceResponse registerDevice(DeviceRegisterRequest request);
    GpsUpdateResponse updateGps(GpsUpdateRequest request);
    void updateFcmToken(String uid, FcmTokenUpdateRequest request);
}
