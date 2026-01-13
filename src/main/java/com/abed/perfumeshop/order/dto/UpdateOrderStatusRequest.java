package com.abed.perfumeshop.order.dto;

import com.abed.perfumeshop.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "order.status.required")
    private OrderStatus status;

    @Size(max = 500, message = "order.status.reason.too.long")
    private String cancellationReason;

}
