package com.abed.perfumeshop.notification.dto.response;

import com.abed.perfumeshop.common.enums.UserType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@SuperBuilder
public class PushNotificationDTO extends NotificationDTO{

    private UserType targetUserType;

    private Long specificUserId;

    private Map<String, String> data;

    private String imageUrl;

}
