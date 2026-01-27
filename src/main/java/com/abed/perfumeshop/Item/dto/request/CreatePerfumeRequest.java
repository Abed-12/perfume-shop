package com.abed.perfumeshop.Item.dto.request;

import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CreatePerfumeRequest {

    @NotBlank(message = "{perfume.name.required}")
    private String name;

    @NotBlank(message = "{perfume.brand.required}")
    private String brand;

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
    private List<CreatePerfumePriceRequest> prices;

    @NotNull(message = "{image.primary.required}")
    @Min(value = 0, message = "{image.primary.index.invalid}")
    private Integer primaryImageIndex;

    @NotEmpty(message = "{image.order.required}")
    private List<Integer> imageOrder;

}
