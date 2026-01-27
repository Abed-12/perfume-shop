package com.abed.perfumeshop.notification.dto.request;

import com.abed.perfumeshop.common.enums.DeviceType;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenDTO {

    private String token;

    private DeviceType deviceType;

    private String deviceName;

}
