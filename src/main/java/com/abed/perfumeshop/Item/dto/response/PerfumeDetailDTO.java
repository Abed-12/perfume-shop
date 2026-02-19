package com.abed.perfumeshop.Item.dto.response;

import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import com.abed.perfumeshop.common.enums.PerfumeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PerfumeDetailDTO {
    private Long id;

    private String name;
    private String brand;
    private Boolean active;

    private Map<String, String> translatedName;
    private Map<String, String> description;

    private PerfumeType perfumeType;
    private List<PerfumeSeason> perfumeSeason;

    private String primaryImageUrl;
    private List<String> imageUrls;

    private List<SizeOptionDTO> availableSizes;

    @Data
    @Builder
    public static class SizeOptionDTO {
        private PerfumeSize size;
        private BigDecimal price;
        private Integer quantity;
        private Boolean available;
    }

}
