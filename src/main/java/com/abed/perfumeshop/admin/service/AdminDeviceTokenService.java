package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.notification.dto.request.DeviceTokenDTO;
import com.abed.perfumeshop.notification.dto.response.DeviceTokenResponseDTO;

import java.util.List;

public interface AdminDeviceTokenService {

    void registerToken(DeviceTokenDTO deviceTokenDTO);

    List<DeviceTokenResponseDTO> getUserDevices();

    void removeDevice(Long deviceId);

}
