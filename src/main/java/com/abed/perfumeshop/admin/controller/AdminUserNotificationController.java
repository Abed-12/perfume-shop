package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminUserNotificationService;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.notification.dto.response.UserNotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminUserNotificationController {

    private final AdminUserNotificationService adminNotificationService;

    @GetMapping
    public ResponseEntity<Response<List<UserNotificationDTO>>> getNotifications() {
        List<UserNotificationDTO> notifications = adminNotificationService.getUserNotifications();

        return ResponseEntity.ok(Response.<List<UserNotificationDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("notifications.retrieved.successfully")
                .data(notifications)
                .build());
    }

    @GetMapping("/has-unread")
    public ResponseEntity<Response<Boolean>> hasUnreadNotifications() {
        boolean hasUnread = adminNotificationService.hasUnreadNotifications();

        return ResponseEntity.ok(Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .data(hasUnread)
                .build());
    }

    @PutMapping("/seen-all")
    public ResponseEntity<Response<Void>> markAllAsSeen() {
        adminNotificationService.markAllAsSeen();

        return ResponseEntity.ok(Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("all.notifications.marked.seen")
                .build());
    }

    @DeleteMapping("/{userNotificationId}")
    public ResponseEntity<Response<Void>> deleteNotification(@PathVariable Long userNotificationId) {
        adminNotificationService.deleteNotification(userNotificationId);

        return ResponseEntity.ok(Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("notification.deleted.successfully")
                .build());
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Response<Void>> clearAll() {
        adminNotificationService.clearAll();

        return ResponseEntity.ok(Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("notifications.cleared.successfully")
                .build());
    }

}
