package com.abed.perfumeshop.Item.dto.request;

import com.abed.perfumeshop.common.enums.PerfumeSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePerfumePriceRequest {

    @NotNull(message = "{perfume.size.required}")
    private PerfumeSize perfumeSize;

    @NotNull(message = "{perfume.price.required}")
    @Positive(message = "{perfume.price.positive}")
    private BigDecimal price;

    @Size(max = 500, message = "{perfume.note.size}")
    private String note;

    @NotNull(message = "{perfume.quantity.required}")
    @Min(value = 0, message = "{perfume.quantity.cannot.be.negative}")
    private Integer quantity;

    @NotNull(message = "{perfume.isActive.required}")
    private Boolean isActive;

}
