package com.abed.perfumeshop.Item.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminPerfumeCardDTO {

    private Long id;

    private String name;
    private String translatedName;
    private String brand;
    private Boolean active;
    private Integer quantity;

    private String primaryImageUrl;

    private BigDecimal currentPrice;

}
