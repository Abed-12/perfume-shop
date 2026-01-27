package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminUserNotificationService;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.notification.dto.response.UserNotificationDTO;
import com.abed.perfumeshop.notification.helper.UserNotificationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserNotificationServiceImpl implements AdminUserNotificationService {

    private final UserNotificationHelper userNotificationHelper;
    private final AdminHelper adminHelper;

    @Override
    public List<UserNotificationDTO> getUserNotifications() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        return userNotificationHelper.getUserNotifications(admin.getId(), UserType.ADMIN);
    }

    @Override
    public boolean hasUnreadNotifications() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        return userNotificationHelper.hasUnreadNotifications(admin.getId(), UserType.ADMIN);
    }

    @Override
    public void markAllAsSeen() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        userNotificationHelper.markAllAsSeen(admin.getId(), UserType.ADMIN);
    }

    @Override
    public void deleteNotification(Long userNotificationId) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        userNotificationHelper.deleteNotification(userNotificationId, admin.getId(), UserType.ADMIN);
    }

    @Override
    public void clearAll() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        userNotificationHelper.clearAll(admin.getId(), UserType.ADMIN);
    }

}
