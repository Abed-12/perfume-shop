package com.abed.perfumeshop.order.helper;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.entity.ItemTranslation;
import com.abed.perfumeshop.Item.entity.Perfume;
import com.abed.perfumeshop.Item.entity.PerfumeImage;
import com.abed.perfumeshop.Item.repo.ItemTranslationRepo;
import com.abed.perfumeshop.Item.repo.PerfumeImageRepo;
import com.abed.perfumeshop.Item.repo.PerfumeRepo;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.order.dto.response.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.entity.CustomerOrder;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.entity.OrderItem;
import com.abed.perfumeshop.order.repo.OrderItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderDetailBuilder {

    private static final String BASE_IMAGE_URL = "/api/public/perfumes";

    private final OrderItemRepo orderItemRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeRepo perfumeRepo;
    private final PerfumeImageRepo perfumeImageRepo;
    private final EnumLocalizationService enumLocalizationService;

    public CustomerOrderDetailDTO buildCustomerOrderDetail(CustomerOrder customerOrder) {
        Order order = customerOrder.getOrder();
        Coupon coupon = customerOrder.getCoupon();
        Customer customer = customerOrder.getCustomer();

        // Fetch order items
        List<OrderItem> orderItems = orderItemRepo.findByOrder_OrderNumber(order.getOrderNumber());

        // Extract item IDs
        List<Long> itemIds = orderItems.stream()
                .map(oi -> oi.getItem().getId())
                .toList();

        // Fetch translations
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIdsAndLocale(
                itemIds,
                LocaleContextHolder.getLocale().getLanguage()
        );
        Map<Long, ItemTranslation> translationsByItem = allTranslations.stream()
                .collect(Collectors.toMap(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        t -> t
                ));

        // Fetch perfumes
        List<Perfume> allPerfumes = perfumeRepo.findByItemIds(itemIds);
        Map<Long, Perfume> perfumesByItem = allPerfumes.stream()
                .collect(Collectors.toMap(
                        perfume -> perfume.getItem().getId(),
                        p -> p
                ));

        // Fetch primary images
        List<Long> perfumeIds = allPerfumes.stream()
                .map(Perfume::getId)
                .toList();

        List<PerfumeImage> primaryImages = perfumeImageRepo
                .findPrimaryImagesByPerfumeIds(perfumeIds);

        Map<Long, PerfumeImage> imagesByPerfume = primaryImages.stream()
                .collect(Collectors.toMap(
                        img -> img.getPerfume().getId(),
                        img -> img
                ));

        // Build order items with calculations
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CustomerOrderDetailDTO.OrderItemInfo> orderItemInfos = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            ItemTranslation itemTranslation = translationsByItem.get(item.getId());
            Perfume perfume = perfumesByItem.get(item.getId());
            PerfumeImage primaryImage = imagesByPerfume.get(perfume.getId());

            // Calculate item subtotal
            BigDecimal itemSubtotal = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            // Build order item info
            CustomerOrderDetailDTO.OrderItemInfo orderItemInfo = CustomerOrderDetailDTO.OrderItemInfo.builder()
                    .itemId(item.getId())
                    .name(item.getName())
                    .translatedName(itemTranslation.getName())
                    .brand(item.getBrand())
                    .quantity(orderItem.getQuantity())
                    .size(enumLocalizationService.getLocalizedName(orderItem.getPerfumeSize()))
                    .unitPrice(orderItem.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId())
                    .build();

            orderItemInfos.add(orderItemInfo);
        }

        // Build customer info
        CustomerOrderDetailDTO.CustomerInfo customerInfo = CustomerOrderDetailDTO.CustomerInfo.builder()
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .build();

        // Build shipping info
        CustomerOrderDetailDTO.ShippingInfo shippingInfo = CustomerOrderDetailDTO.ShippingInfo.builder()
                .phoneNumber(customerOrder.getPhoneNumber())
                .alternativePhoneNumber(customerOrder.getAlternativePhoneNumber())
                .governorate(enumLocalizationService.getLocalizedName(customerOrder.getGovernorate()))
                .address(customerOrder.getAddress())
                .build();

        // Build coupon info if exists
        CustomerOrderDetailDTO.CouponInfo couponInfo = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (coupon != null) {
            couponInfo = CustomerOrderDetailDTO.CouponInfo.builder()
                    .code(coupon.getCode())
                    .discountType(enumLocalizationService.getLocalizedName(coupon.getDiscountType()))
                    .discountValue(coupon.getDiscountValue())
                    .build();

            discountAmount = calculateDiscount(subtotal, coupon);
        }

        // Build pricing info
        CustomerOrderDetailDTO.PricingInfo pricingInfo = CustomerOrderDetailDTO.PricingInfo.builder()
                .subtotal(subtotal)
                .shippingFee(order.getShippingFee())
                .discountAmount(discountAmount)
                .totalPrice(order.getTotalPrice())
                .build();

        // Build final response
        return CustomerOrderDetailDTO.builder()
                .orderNumber(order.getOrderNumber())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .notes(order.getNotes())
                .orderDate(order.getOrderDate())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .customerInfo(customerInfo)
                .shippingInfo(shippingInfo)
                .couponInfo(couponInfo)
                .items(orderItemInfos)
                .pricing(pricingInfo)
                .build();
    }

    public GuestOrderDetailDTO buildGuestOrderDetail(GuestOrder guestOrder) {
        Order order = guestOrder.getOrder();

        // Fetch order items
        List<OrderItem> orderItems = orderItemRepo.findByOrder_OrderNumber(order.getOrderNumber());

        // Extract item IDs
        List<Long> itemIds = orderItems.stream()
                .map(oi -> oi.getItem().getId())
                .toList();

        // Fetch translations
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIdsAndLocale(
                itemIds,
                LocaleContextHolder.getLocale().getLanguage()
        );
        Map<Long, ItemTranslation> translationsByItem = allTranslations.stream()
                .collect(Collectors.toMap(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        t -> t
                ));

        // Fetch perfumes
        List<Perfume> allPerfumes = perfumeRepo.findByItemIds(itemIds);
        Map<Long, Perfume> perfumesByItem = allPerfumes.stream()
                .collect(Collectors.toMap(
                        perfume -> perfume.getItem().getId(),
                        p -> p
                ));

        // Fetch primary images
        List<Long> perfumeIds = allPerfumes.stream()
                .map(Perfume::getId)
                .toList();

        List<PerfumeImage> primaryImages = perfumeImageRepo
                .findPrimaryImagesByPerfumeIds(perfumeIds);

        Map<Long, PerfumeImage> imagesByPerfume = primaryImages.stream()
                .collect(Collectors.toMap(
                        img -> img.getPerfume().getId(),
                        img -> img
                ));

        // Build order items with calculations
        BigDecimal subtotal = BigDecimal.ZERO;
        List<GuestOrderDetailDTO.OrderItemInfo> orderItemInfos = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            ItemTranslation itemTranslation = translationsByItem.get(item.getId());
            Perfume perfume = perfumesByItem.get(item.getId());
            PerfumeImage primaryImage = imagesByPerfume.get(perfume.getId());

            // Calculate item subtotal
            BigDecimal itemSubtotal = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            // Build order item info
            GuestOrderDetailDTO.OrderItemInfo orderItemInfo = GuestOrderDetailDTO.OrderItemInfo.builder()
                    .itemId(item.getId())
                    .name(item.getName())
                    .translatedName(itemTranslation.getName())
                    .brand(item.getBrand())
                    .quantity(orderItem.getQuantity())
                    .size(enumLocalizationService.getLocalizedName(orderItem.getPerfumeSize()))
                    .unitPrice(orderItem.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId())
                    .build();

            orderItemInfos.add(orderItemInfo);
        }

        // Build guest info
        GuestOrderDetailDTO.GuestInfo guestInfo = GuestOrderDetailDTO.GuestInfo.builder()
                .username(guestOrder.getUsername())
                .email(guestOrder.getEmail())
                .build();

        // Build shipping info
        GuestOrderDetailDTO.ShippingInfo shippingInfo = GuestOrderDetailDTO.ShippingInfo.builder()
                .phoneNumber(guestOrder.getPhoneNumber())
                .alternativePhoneNumber(guestOrder.getAlternativePhoneNumber())
                .governorate(enumLocalizationService.getLocalizedName(guestOrder.getGovernorate()))
                .address(guestOrder.getAddress())
                .build();

        // Build pricing info
        GuestOrderDetailDTO.PricingInfo pricingInfo = GuestOrderDetailDTO.PricingInfo.builder()
                .subtotal(subtotal)
                .shippingFee(order.getShippingFee())
                .totalPrice(order.getTotalPrice())
                .build();

        // Build final response
        return GuestOrderDetailDTO.builder()
                .orderNumber(order.getOrderNumber())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .notes(order.getNotes())
                .orderDate(order.getOrderDate())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .guestInfo(guestInfo)
                .shippingInfo(shippingInfo)
                .items(orderItemInfos)
                .pricing(pricingInfo)
                .build();
    }

    // ========== Private Helper Methods ==========
    private BigDecimal calculateDiscount(BigDecimal subtotal, Coupon coupon) {
        return switch (coupon.getDiscountType()) {
            case PERCENTAGE -> subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> coupon.getDiscountValue().min(subtotal);
        };
    }

}
