package com.abed.perfumeshop.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponValidationResponse {

    private String couponCode;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal originalPrice;

    private BigDecimal discountAmount;

    private BigDecimal finalPrice;

}
