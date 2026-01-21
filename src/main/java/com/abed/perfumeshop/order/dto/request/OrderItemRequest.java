package com.abed.perfumeshop.order.dto.request;

import com.abed.perfumeshop.common.enums.PerfumeSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "{order.item.id.required}")
    private Long itemId;

    @NotNull(message = "{perfume.size.required}")
    private PerfumeSize perfumeSize;

    @NotNull(message = "{order.item.quantity.required}")
    @Min(value = 1, message = "{order.item.quantity.min}")
    private Integer quantity;

}
