package com.abed.perfumeshop.notification.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@SuperBuilder
public class EmailNotificationDTO extends NotificationDTO {

    private String recipient;

    // For values/variables to be passed into email template to send
    private String templateName;
    private Map<String, Object> templateVariables;

}
