package com.abed.perfumeshop.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryDTO {

    private String orderNumber;
    private String status;
    private String orderType;
    private LocalDateTime orderDate;

    private String customerName;

    private BigDecimal totalPrice;
    private Integer itemCount;

    private String guestEmail;

}
