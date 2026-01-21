package com.abed.perfumeshop.Item.dto.request;

import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UpdatePerfumeRequest {

    @NotBlank(message = "{perfume.name.required}")
    private String name;

    @NotBlank(message = "{perfume.brand.required}")
    private String brand;

    @NotNull(message = "{perfume.active.required}")
    private Boolean active;

    @Valid
    @NotEmpty(message = "{item.translations.required}")
    private List<ItemTranslationRequest> translations;

    @NotNull(message = "{perfume.type.required}")
    private PerfumeType perfumeType;

    @NotEmpty(message = "{perfume.seasons.required}")
    @Size(min = 1, max = 4, message = "{perfume.seasons.size}")
    private Set<PerfumeSeason> perfumeSeasons;

    @Valid
    @NotEmpty(message = "{perfume.prices.required}")
    private List<UpdatePerfumePriceRequest> prices;

}
