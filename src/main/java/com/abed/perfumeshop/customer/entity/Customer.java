package com.abed.perfumeshop.customer.entity;

import com.abed.perfumeshop.common.enums.Governorate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "customers")
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    private String alternativePhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Governorate governorate;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

}
