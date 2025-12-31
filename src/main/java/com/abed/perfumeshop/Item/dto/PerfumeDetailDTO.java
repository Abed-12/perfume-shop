package com.abed.perfumeshop.Item.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PerfumeDetailDTO {
    private Long id;

    private String name;
    private Integer quantity;
    private String brand;
    private Boolean active;

    private BigDecimal currentPrice;

    private String translatedName;
    private String description;

    private String size;
    private String perfumeType;
    private String perfumeSeason;

    private List<String> imageUrls;
}
