package com.abed.perfumeshop.notification.helper;

import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.notification.dto.request.DeviceTokenDTO;
import com.abed.perfumeshop.notification.dto.response.DeviceTokenResponseDTO;
import com.abed.perfumeshop.notification.entity.DeviceToken;
import com.abed.perfumeshop.notification.repo.DeviceTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DeviceTokenHelper {

    private final DeviceTokenRepo deviceTokenRepo;

    @Transactional
    public void registerToken(DeviceTokenDTO deviceTokenDTO, Long userId, UserType userType) {
        // Skip if token already registered
        if (deviceTokenRepo.findByToken(deviceTokenDTO.getToken()).isPresent()) {
            return;
        }

        DeviceToken deviceToken = DeviceToken.builder()
                .token(deviceTokenDTO.getToken())
                .userType(userType)
                .userId(userId)
                .deviceType(deviceTokenDTO.getDeviceType())
                .deviceName(deviceTokenDTO.getDeviceName())
                .build();

        deviceTokenRepo.save(deviceToken);
    }

    public List<DeviceTokenResponseDTO> getUserDevices(Long userId, UserType userType) {
        List<DeviceToken> tokens = deviceTokenRepo.findByUserIdAndUserType(userId, userType);

        return tokens.stream()
                .map(this::mapToDeviceTokenResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeDevice(Long userId, UserType userType, Long deviceId) {
        DeviceToken deviceToken = deviceTokenRepo.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("device.token.not.found", new Object[]{deviceId}));

        // Verify device belongs to requesting user
        if (!deviceToken.getUserId().equals(userId) || !deviceToken.getUserType().equals(userType)) {
            throw new NotFoundException("device.token.not.found", new Object[]{deviceId});
        }

        deviceTokenRepo.delete(deviceToken);
    }

    // ========== Private Helper Methods ==========
    private DeviceTokenResponseDTO mapToDeviceTokenResponseDTO(DeviceToken deviceToken) {
        return DeviceTokenResponseDTO.builder()
                .id(deviceToken.getId())
                .deviceName(deviceToken.getDeviceName())
                .deviceType(deviceToken.getDeviceType().name())
                .createdAt(deviceToken.getCreatedAt())
                .build();
    }

}
