package com.abed.perfumeshop.delivery.entity;

import com.abed.perfumeshop.common.enums.Governorate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@Table(name = "delivery_fees")
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Governorate governorate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

}
