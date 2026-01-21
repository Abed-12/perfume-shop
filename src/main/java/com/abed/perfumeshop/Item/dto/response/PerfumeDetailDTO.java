package com.abed.perfumeshop.Item.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PerfumeDetailDTO {
    private Long id;

    private String name;
    private String brand;
    private Boolean active;

    private String translatedName;
    private String description;

    private String perfumeType;
    private String perfumeSeason;

    private List<String> imageUrls;

    private List<SizeOptionDTO> availableSizes;

    @Data
    @Builder
    public static class SizeOptionDTO {
        private String size;
        private BigDecimal price;
        private Integer quantity;
        private Boolean available;
    }

}
