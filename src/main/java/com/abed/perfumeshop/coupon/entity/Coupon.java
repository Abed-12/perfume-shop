package com.abed.perfumeshop.coupon.entity;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.common.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "coupons")
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DiscountType discountType;

    @Column(nullable = false, updatable = false)
    private Double discountValue;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer usageCount = 0;

    private Integer maxUsage;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Admin createdBy;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
