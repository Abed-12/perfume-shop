package com.abed.perfumeshop.delivery.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDeliveryFeeRequest {

    @NotNull(message = "{delivery.fee.price.required}")
    @Positive(message = "{delivery.fee.price.positive}")
    private BigDecimal shippingFee;

    @NotNull(message = "{delivery.fee.active.required}")
    private Boolean active;

}
