package com.abed.perfumeshop.Item.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class AdminPerfumeCardDTO {

    private Long id;

    private String name;
    private Map<String, String> translatedName;
    private String brand;
    private Boolean active;
    private Integer quantity;

    private String primaryImageUrl;

    private BigDecimal lowestPrice;

}
