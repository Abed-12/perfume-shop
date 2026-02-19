package com.abed.perfumeshop.order.dto.response;

import com.abed.perfumeshop.common.enums.DiscountType;
import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CustomerOrderDetailDTO {

    private String orderNumber;

    private String status;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private CustomerInfo customerInfo;

    private ShippingInfo shippingInfo;

    private CouponInfo couponInfo;

    private List<OrderItemInfo> items;

    private PricingInfo pricing;

    @Data
    @Builder
    public static class CustomerInfo {
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    @Builder
    public static class ShippingInfo {
        private String phoneNumber;
        private String alternativePhoneNumber;
        private Governorate governorate;
        private String address;
    }

    @Data
    @Builder
    public static class CouponInfo {
        private String code;
        private DiscountType discountType;
        private BigDecimal discountValue;
    }

    @Data
    @Builder
    public static class OrderItemInfo {
        private Long itemId;
        private String name;
        private Map<String, String> translatedName;
        private String brand;
        private Integer quantity;
        private PerfumeSize size;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String primaryImageUrl;
    }

    @Data
    @Builder
    public static class PricingInfo {
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private BigDecimal totalPrice;
    }

}
