package com.abed.perfumeshop.notification.service.impl;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.dto.response.PushNotificationDTO;
import com.abed.perfumeshop.notification.entity.DeviceToken;
import com.abed.perfumeshop.notification.entity.UserNotification;
import com.abed.perfumeshop.notification.repo.DeviceTokenRepo;
import com.abed.perfumeshop.notification.repo.NotificationRepo;
import com.abed.perfumeshop.notification.repo.UserNotificationRepo;
import com.abed.perfumeshop.notification.service.NotificationSender;
import com.abed.perfumeshop.notification.entity.Notification;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushNotificationSender implements NotificationSender {

    private final DeviceTokenRepo deviceTokenRepo;
    private final NotificationRepo notificationRepo;
    private final UserNotificationRepo userNotificationRepo;

    @Override
    @Async
    public void send(NotificationDTO notificationDTO) {
        // Validate DTO type
        if (!(notificationDTO instanceof PushNotificationDTO pushNotificationDTO)) {
            log.error("Invalid DTO type for PushNotificationSender. Expected PushNotificationDTO but got {}",
                    notificationDTO.getClass().getSimpleName());
            return;
        }

        try {
            // Get target device tokens
            List<DeviceToken> tokens = getTargetTokens(pushNotificationDTO);

            if (tokens.isEmpty()) {
                log.warn("No device tokens found for push notification");
                return;
            }

            // Build Firebase notification
            com.google.firebase.messaging.Notification notification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(pushNotificationDTO.getSubject())
                            .setBody(pushNotificationDTO.getBody())
                            .setImage(pushNotificationDTO.getImageUrl())
                            .build();

            // Send to all devices
            for (DeviceToken deviceToken : tokens) {
                sendToDevice(deviceToken.getToken(), notification, pushNotificationDTO);
            }

            // Save notification ONCE
            Notification savedNotification = saveNotification(pushNotificationDTO, tokens);

            // Save notification once per unique user
            List<UserNotification> userNotifications = tokens.stream()
                    .map(token -> Map.entry(token.getUserId(), token.getUserType()))
                    .distinct()
                    .map(entry -> UserNotification.builder()
                            .userType(entry.getValue())
                            .userId(entry.getKey())
                            .notification(savedNotification)
                            .build())
                    .collect(Collectors.toList());

            userNotificationRepo.saveAll(userNotifications);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

    // ========== Private Helper Methods ==========
    private List<DeviceToken> getTargetTokens(PushNotificationDTO pushNotificationDTO) {
        if (pushNotificationDTO.getSpecificUserId() != null && pushNotificationDTO.getTargetUserType() != null) {
            return deviceTokenRepo.findByUserIdAndUserType(pushNotificationDTO.getSpecificUserId(), pushNotificationDTO.getTargetUserType());
        } else if (pushNotificationDTO.getTargetUserType() != null) {
            return deviceTokenRepo.findByUserType(pushNotificationDTO.getTargetUserType());
        } else {
            return deviceTokenRepo.findAll();
        }
    }

    private void sendToDevice(
            String token,
            com.google.firebase.messaging.Notification notification,
            PushNotificationDTO pushNotificationDTO
    ) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putAllData(pushNotificationDTO.getData() != null
                            ? pushNotificationDTO.getData()
                            : new HashMap<>())
                    .build();

            // Send push
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            // Auto-cleanup: Remove invalid or unregistered tokens
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                deviceTokenRepo.findByToken(token).ifPresent(deviceTokenRepo::delete);
            }
        }
    }

    private Notification saveNotification(
            PushNotificationDTO pushNotificationDTO,
            List<DeviceToken> tokens
    ) {
        // Build recipient string: "1 (ADMIN), 2 (ADMIN), 3 (ADMIN)"
        String recipient = tokens.stream()
                .map(token -> Map.entry(token.getUserId(), token.getUserType()))
                .distinct()
                .map(entry -> entry.getKey() + " (" + entry.getValue().name() + ")")
                .collect(Collectors.joining(", "));

        Notification notification = Notification.builder()
                .subject(pushNotificationDTO.getSubject())
                .recipient(recipient)
                .body(pushNotificationDTO.getBody())
                .type(NotificationType.PUSH)
                .order(pushNotificationDTO.getOrder())
                .coupon(pushNotificationDTO.getCoupon())
                .build();

        return notificationRepo.save(notification);
    }

}
