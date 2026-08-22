package com.abed.perfumeshop.delivery.dto;

import com.abed.perfumeshop.common.enums.Governorate;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeliveryFeeDTO {

    private Governorate governorate;
    private BigDecimal shippingFee;

}
