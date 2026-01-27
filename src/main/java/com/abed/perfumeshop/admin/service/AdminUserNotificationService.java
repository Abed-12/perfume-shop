package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.notification.dto.response.UserNotificationDTO;

import java.util.List;

public interface AdminUserNotificationService {

    List<UserNotificationDTO> getUserNotifications();

    boolean hasUnreadNotifications();

    void markAllAsSeen();

    void deleteNotification(Long userNotificationId);

    void clearAll();

}
