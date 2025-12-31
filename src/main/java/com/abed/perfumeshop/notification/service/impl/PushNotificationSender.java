package com.abed.perfumeshop.notification.service.impl;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSender;
import com.abed.perfumeshop.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationDTO notificationDTO, Order order) {
        System.out.println("Push");
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

}
