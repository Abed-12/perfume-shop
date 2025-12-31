package com.abed.perfumeshop.Item.dto;

import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import com.abed.perfumeshop.common.enums.PerfumeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePerfumeRequest {

    @NotBlank(message = "{perfume.name.required}")
    private String name;

    @NotNull(message = "{perfume.quantity.required}")
    @Min(value = 0, message = "{perfume.quantity.min}")
    private Integer quantity;

    @NotBlank(message = "{perfume.brand.required}")
    private String brand;

    @NotNull(message = "{perfume.active.required}")
    private Boolean active;

    @NotNull(message = "{perfume.price.required}")
    @Positive(message = "{perfume.price.positive}")
    private BigDecimal price;

    @Size(max = 500, message = "{perfume.note.size}")
    private String note;

    @Valid
    @NotEmpty(message = "{item.translations.required}")
    private List<ItemTranslationRequest> translations;

    @NotNull(message = "{perfume.size.required}")
    private PerfumeSize perfumeSize;

    @NotNull(message = "{perfume.type.required}")
    private PerfumeType perfumeType;

    @NotNull(message = "{perfume.season.required}")
    private PerfumeSeason perfumeSeason;

}
