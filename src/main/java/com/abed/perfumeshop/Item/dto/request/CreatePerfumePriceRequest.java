package com.abed.perfumeshop.Item.dto.request;

import com.abed.perfumeshop.common.enums.PerfumeSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePerfumePriceRequest {

    @NotNull(message = "{perfume.size.required}")
    private PerfumeSize perfumeSize;

    @NotNull(message = "{perfume.price.required}")
    @Positive(message = "{perfume.price.positive}")
    private BigDecimal price;

    @NotNull(message = "{perfume.quantity.required}")
    @Min(value = 1, message = "{perfume.quantity.min}")
    private Integer quantity;

}
