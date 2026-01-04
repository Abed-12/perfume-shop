package com.abed.perfumeshop.order.service.impl;

import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.*;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.OutOfStockException;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.GuestOrderResponseDTO;
import com.abed.perfumeshop.order.dto.OrderItemRequest;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.entity.OrderItem;
import com.abed.perfumeshop.order.repo.GuestOrderRepo;
import com.abed.perfumeshop.order.repo.OrderItemRepo;
import com.abed.perfumeshop.order.repo.OrderRepo;
import com.abed.perfumeshop.order.service.GuestOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService {

    private static final String BASE_IMAGE_URL = "/api/public/perfumes";

    private final GuestOrderRepo guestOrderRepo;
    private final OrderRepo orderRepo;
    private final ItemRepo itemRepo;
    private final OrderItemRepo orderItemRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final CustomerRepo customerRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeRepo perfumeRepo;
    private final PerfumeImageRepo perfumeImageRepo;
    private final NotificationSenderFacade notificationSenderFacade;
    private final MessageSource messageSource;
    private final EnumLocalizationService enumLocalizationService;

    @Value("${guest.order.tracking.link}")
    private String guestOrderTrackingLink;

    @Override
    @Transactional
    public Response<GuestOrderResponseDTO> createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest) {
        // Calculate shipping fee and initialize subtotal
        BigDecimal shippingFee = createGuestOrderRequest.getGovernorate().getShippingFee();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Create and save the order
        Order order = Order.builder()
                .notes(createGuestOrderRequest.getNotes())
                .shippingFee(shippingFee)
                .build();
        order = orderRepo.save(order);

        // Process each item in the order
        for (OrderItemRequest orderItemRequest : createGuestOrderRequest.getItems()){
            Item item = itemRepo.findById(orderItemRequest.getItemId())
                    .orElseThrow(() -> new NotFoundException("item.not.found"));

            // Validate item availability
            if (!item.getActive()) {
                throw new OutOfStockException("item.inactive", new Object[]{item.getName()});
            }

            if (item.getQuantity() < orderItemRequest.getQuantity()){
                throw new OutOfStockException(
                        "item.insufficient.stock",
                        new Object[]{
                                item.getName(),
                                item.getQuantity(),
                                orderItemRequest.getQuantity()
                        }
                );
            }

            // Get current item price
            ItemPrice itemPrice = itemPriceRepo.findCurrentActivePriceByItemId(item.getId())
                    .orElseThrow(() -> new NotFoundException("item.price.not.found"));

            // Update item quantity
            int newQuantity = item.getQuantity() - orderItemRequest.getQuantity();
            item.setQuantity(newQuantity);

            if (newQuantity == 0) {
                item.setActive(false);
            }

            itemRepo.save(item);

            // Create and save order item
            OrderItem orderItem = OrderItem.builder()
                    .quantity(orderItemRequest.getQuantity())
                    .unitPrice(itemPrice.getPrice())
                    .order(order)
                    .item(item)
                    .build();
            orderItemRepo.save(orderItem);

            // Calculate subtotal
            BigDecimal itemSubtotal = itemPrice.getPrice()
                    .multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        // Update order with total price
        order.setTotalPrice(subtotal.add(shippingFee));

        // Generate tracking token
        String trackingToken = UUID.randomUUID().toString();

        // Check if customer exists with same email (auto-linking)
        Optional<Customer> existingCustomer = customerRepo.findByEmail(createGuestOrderRequest.getEmail());

        // Create and save guest order
        GuestOrder guestOrder = GuestOrder.builder()
                .username(createGuestOrderRequest.getUsername())
                .email(createGuestOrderRequest.getEmail())
                .phoneNumber(createGuestOrderRequest.getPhoneNumber())
                .alternativePhoneNumber(createGuestOrderRequest.getAlternativePhoneNumber())
                .governorate(createGuestOrderRequest.getGovernorate())
                .address(createGuestOrderRequest.getAddress())
                .trackingToken(trackingToken)
                .claimedByCustomer(existingCustomer.orElse(null))
                .claimedAt(existingCustomer.isPresent() ? LocalDateTime.now() : null)
                .order(order)
                .build();
        guestOrderRepo.save(guestOrder);

        // Send email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", createGuestOrderRequest.getUsername());
        templateVariables.put("orderId", order.getId());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("trackingLink", guestOrderTrackingLink + trackingToken);
        templateVariables.put("trackingToken", trackingToken);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(createGuestOrderRequest.getEmail())
                .subject(messageSource.getMessage("notification.order.confirmation.subject", null, LocaleContextHolder.getLocale()))
                .order(order)
                .templateName("guest-order-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO, order);

        return Response.<GuestOrderResponseDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("order.created")
                .data(GuestOrderResponseDTO.builder()
                        .trackingToken(trackingToken)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<GuestOrderDetailDTO> getGuestOrderByToken(String trackingToken) {
        // Fetch guest order by tracking token
        GuestOrder guestOrder = guestOrderRepo.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        Order order = guestOrder.getOrder();

        // Fetch order items
        List<OrderItem> orderItems = orderItemRepo.findByOrder_Id(order.getId());

        // Extract item IDs for bulk queries
        List<Long> itemIds = orderItems.stream()
                .map(oi -> oi.getItem().getId())
                .toList();

        // Bulk fetch translations
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIdsAndLocale(
                itemIds,
                LocaleContextHolder.getLocale().getLanguage()
        );
        Map<Long, ItemTranslation> translationsByItem = allTranslations.stream()
                .collect(Collectors.toMap(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        t -> t
                ));

        // Bulk fetch perfumes
        List<Perfume> allPerfumes = perfumeRepo.findByItemIds(itemIds);
        Map<Long, Perfume> perfumesByItem = allPerfumes.stream()
                .collect(Collectors.toMap(
                        perfume -> perfume.getItem().getId(),
                        p -> p
                ));

        // Bulk fetch primary images
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
                    .size(enumLocalizationService.getLocalizedName(perfume.getPerfumeSize()))
                    .unitPrice(orderItem.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId())
                    .build();

            orderItemInfos.add(orderItemInfo);
        }

        // Build customer info
        GuestOrderDetailDTO.CustomerInfo customerInfo = GuestOrderDetailDTO.CustomerInfo.builder()
                .username(guestOrder.getUsername())
                .email(guestOrder.getEmail())
                .phoneNumber(guestOrder.getPhoneNumber())
                .alternativePhoneNumber(guestOrder.getAlternativePhoneNumber())
                .governorate(enumLocalizationService.getLocalizedName(guestOrder.getGovernorate()))
                .address(guestOrder.getAddress())
                .build();

        // Calculate pricing
        BigDecimal shippingFee = guestOrder.getGovernorate().getShippingFee();
        BigDecimal totalPrice = subtotal.add(shippingFee);

        GuestOrderDetailDTO.PricingInfo pricingInfo = GuestOrderDetailDTO.PricingInfo.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .totalPrice(totalPrice)
                .build();

        // Build final response
        GuestOrderDetailDTO guestOrderDetailDTO = GuestOrderDetailDTO.builder()
                .orderId(order.getId())
                .trackingToken(guestOrder.getTrackingToken())
                .status(order.getStatus())
                .notes(order.getNotes())
                .orderDate(order.getOrderDate())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .customerInfo(customerInfo)
                .items(orderItemInfos)
                .pricing(pricingInfo)
                .build();

        return Response.<GuestOrderDetailDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("order.retrieved.successfully")
                .data(guestOrderDetailDTO)
                .build();
    }

}
