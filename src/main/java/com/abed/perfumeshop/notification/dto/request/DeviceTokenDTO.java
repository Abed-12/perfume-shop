package com.abed.perfumeshop.notification.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenDTO {

    private String token;
    private String deviceType;
    private String deviceName;

}
