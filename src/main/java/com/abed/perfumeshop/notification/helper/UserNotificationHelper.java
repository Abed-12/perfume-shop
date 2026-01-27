package com.abed.perfumeshop.notification.helper;

import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.notification.dto.response.UserNotificationDTO;
import com.abed.perfumeshop.notification.entity.Notification;
import com.abed.perfumeshop.notification.entity.UserNotification;
import com.abed.perfumeshop.notification.repo.UserNotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserNotificationHelper {

    private final UserNotificationRepo userNotificationRepo;

    public List<UserNotificationDTO> getUserNotifications(Long userId, UserType userType) {
        List<UserNotification> notifications = userNotificationRepo
                .findByUserIdAndUserTypeOrderByReceivedAtDesc(userId, userType);

        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean hasUnreadNotifications(Long userId, UserType userType) {
        return userNotificationRepo.existsByUserIdAndUserTypeAndSeenFalse(userId, userType);
    }

    @Transactional
    public void markAllAsSeen(Long userId, UserType userType) {
        userNotificationRepo.markAllAsSeen(userId, userType, LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(Long userNotificationId, Long userId, UserType userType) {
        UserNotification notification = userNotificationRepo
                .findByIdAndUserIdAndUserType(userNotificationId, userId, userType)
                .orElseThrow(() -> new NotFoundException("notification.not.found"));

        userNotificationRepo.delete(notification);
    }

    @Transactional
    public void clearAll(Long userId, UserType userType) {
        userNotificationRepo.deleteByUserIdAndUserType(userId, userType);
    }

    // ========== Private Helper Methods ==========
    private UserNotificationDTO mapToDTO(UserNotification userNotification) {
        Notification notification = userNotification.getNotification();
        String orderNumber = notification.getOrder().getOrderNumber();
        String userType = orderNumber.substring(0, 3);

        return UserNotificationDTO.builder()
                .id(userNotification.getId())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .seen(userNotification.getSeen())
                .receivedAt(userNotification.getReceivedAt())
                .seenAt(userNotification.getSeenAt())
                .data(Map.of(
                        "orderNumber", orderNumber,
                        "email", notification.getRecipient()
                ))
                .userType(userType.equals("CUS") ? "CUSTOMER" : "GUEST")
                .build();
    }

}
