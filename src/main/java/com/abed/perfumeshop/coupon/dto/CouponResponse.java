package com.abed.perfumeshop.coupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponse {

    private Long id;

    private String code;

    private String discountType;

    private Double discountValue;

    private LocalDateTime expiryDate;

    private Boolean active;

    private Integer usageCount;

    private Integer maxUsage;

}
