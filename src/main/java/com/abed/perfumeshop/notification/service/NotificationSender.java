package com.abed.perfumeshop.notification.service;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
import com.abed.perfumeshop.order.entity.Order;

public interface NotificationSender {

    void send(NotificationDTO notificationDTO, Order order);

    NotificationType getType();

}
