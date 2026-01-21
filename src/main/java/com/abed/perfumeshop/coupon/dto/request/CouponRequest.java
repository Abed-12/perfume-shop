package com.abed.perfumeshop.coupon.dto.request;

import com.abed.perfumeshop.common.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CouponRequest {

    private Long id;

    @NotNull(message = "{coupon.discountType.required}")
    @JsonProperty(required = true)
    private DiscountType discountType;

    @NotNull(message = "{coupon.discountValue.required}")
    @Positive(message = "{coupon.discountValue.positive}")
    private BigDecimal discountValue;

    @NotNull(message = "{coupon.expiryDate.required}")
    @Future(message = "{coupon.expiryDate.future}")
    private LocalDateTime expiryDate;

    @Min(value = 1, message = "{coupon.maxUsage.min}")
    private Integer maxUsage;

}
