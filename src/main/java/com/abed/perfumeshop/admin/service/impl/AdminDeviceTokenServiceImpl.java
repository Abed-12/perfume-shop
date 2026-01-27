package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminDeviceTokenService;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.notification.dto.request.DeviceTokenDTO;
import com.abed.perfumeshop.notification.dto.response.DeviceTokenResponseDTO;
import com.abed.perfumeshop.notification.helper.DeviceTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDeviceTokenServiceImpl implements AdminDeviceTokenService {

    private final DeviceTokenHelper deviceTokenHelper;
    private final AdminHelper adminHelper;

    @Override
    public void registerToken(DeviceTokenDTO deviceTokenDTO) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        deviceTokenHelper.registerToken(deviceTokenDTO, admin.getId(), UserType.ADMIN);
    }

    @Override
    public List<DeviceTokenResponseDTO> getUserDevices() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        return deviceTokenHelper.getUserDevices(admin.getId(), UserType.ADMIN);
    }

    @Override
    public void removeDevice(Long deviceId) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        deviceTokenHelper.removeDevice(admin.getId(), UserType.ADMIN, deviceId);
    }

}
