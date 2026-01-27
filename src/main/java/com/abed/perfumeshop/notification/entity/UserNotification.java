package com.abed.perfumeshop.notification.entity;

import com.abed.perfumeshop.common.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "user_notifications")
@AllArgsConstructor
@NoArgsConstructor
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserType userType;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private Notification notification;

    @Column(nullable = false)
    @Builder.Default
    private Boolean seen = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime seenAt;

}
