package com.abed.perfumeshop.Item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemTranslationRequest {

    @NotBlank(message = "{item.translation.locale.required}")
    private String locale;

    @NotBlank(message = "{item.translation.name.required}")
    private String name;

    private String description;

}
