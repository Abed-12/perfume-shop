package com.abed.perfumeshop.notification.service;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;

public interface NotificationSender {

    void send(NotificationDTO notificationDTO);

    NotificationType getType();

}
