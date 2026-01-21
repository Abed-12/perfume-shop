package com.abed.perfumeshop.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CouponValidationRequest {

    @NotBlank(message = "{coupon.code.required}")
    private String couponCode;

    @NotNull(message = "{coupon.order.total.required}")
    @Positive(message = "{coupon.order.total.positive}")
    private BigDecimal orderTotal;

}
