package com.abed.perfumeshop.discount.entity;

import com.abed.perfumeshop.common.enums.DiscountType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "global_discounts")
public class GlobalDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active;


}
