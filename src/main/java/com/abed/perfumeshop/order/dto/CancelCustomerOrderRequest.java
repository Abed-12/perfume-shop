package com.abed.perfumeshop.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelCustomerOrderRequest {

    @Size(max = 500, message = "order.cancellation.reason.too.long")
    private String cancellationReason;

}
