package com.abed.perfumeshop.notification.entity;

import com.abed.perfumeshop.common.enums.DeviceType;
import com.abed.perfumeshop.common.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "device_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_device",
                columnNames = {"user_id", "user_type", "device_name"}
        ))
@AllArgsConstructor
@NoArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserType userType;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DeviceType deviceType;

    @Column(nullable = false, updatable = false)
    private String deviceName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
