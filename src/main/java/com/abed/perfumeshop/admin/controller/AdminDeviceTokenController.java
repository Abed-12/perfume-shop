package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminDeviceTokenService;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.notification.dto.request.DeviceTokenDTO;
import com.abed.perfumeshop.notification.dto.response.DeviceTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
public class AdminDeviceTokenController {

    private final AdminDeviceTokenService adminDeviceTokenService;

    @PostMapping
    public ResponseEntity<Response<Void>> registerToken(@RequestBody DeviceTokenDTO deviceTokenDTO) {
        adminDeviceTokenService.registerToken(deviceTokenDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.<Void>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("device.token.registered")
                        .build());
    }

    @GetMapping
    public ResponseEntity<Response<List<DeviceTokenResponseDTO>>> getMyDevices() {
        List<DeviceTokenResponseDTO> devices = adminDeviceTokenService.getUserDevices();

        return ResponseEntity.ok(Response.<List<DeviceTokenResponseDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("device.list.retrieved")
                        .data(devices)
                .build());
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Response<Void>> removeDevice(@PathVariable Long deviceId) {
        adminDeviceTokenService.removeDevice(deviceId);

        return ResponseEntity.ok(Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("device.removed.successfully")
                .build());
    }

}
