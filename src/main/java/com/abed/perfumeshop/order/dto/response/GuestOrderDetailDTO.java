package com.abed.perfumeshop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GuestOrderDetailDTO {

    private String orderNumber;

    private String status;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private GuestInfo guestInfo;

    private ShippingInfo shippingInfo;

    private List<OrderItemInfo> items;

    private PricingInfo pricing;

    @Data
    @Builder
    public static class GuestInfo {
        private String username;
        private String email;
    }

    @Data
    @Builder
    public static class ShippingInfo {
        private String phoneNumber;
        private String alternativePhoneNumber;
        private String governorate;
        private String address;
    }

    @Data
    @Builder
    public static class OrderItemInfo {
        private Long itemId;
        private String name;
        private String translatedName;
        private String brand;
        private Integer quantity;
        private String size;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String primaryImageUrl;
    }

    @Data
    @Builder
    public static class PricingInfo {
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal totalPrice;
    }

}
