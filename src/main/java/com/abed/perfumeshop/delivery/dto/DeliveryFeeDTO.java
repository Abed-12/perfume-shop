package com.abed.perfumeshop.delivery.dto;

import com.abed.perfumeshop.common.enums.Governorate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryFeeDTO {

    private Long id;
    private Governorate governorate;
    private BigDecimal shippingFee;
    private Boolean active;

}
