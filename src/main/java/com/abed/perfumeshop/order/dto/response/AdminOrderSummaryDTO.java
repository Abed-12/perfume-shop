package com.abed.perfumeshop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderSummaryDTO {

    private String orderNumber;
    private String status;
    private LocalDateTime orderDate;

    private String customerName;
    private String customerEmail;

    private BigDecimal totalPrice;
    private Integer itemCount;

}
