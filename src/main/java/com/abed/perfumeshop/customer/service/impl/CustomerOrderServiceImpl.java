package com.abed.perfumeshop.customer.service.impl;

import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.*;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.coupon.entity.CouponUsage;
import com.abed.perfumeshop.coupon.repo.CouponRepo;
import com.abed.perfumeshop.coupon.repo.CouponUsageRepo;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.helper.CustomerHelper;
import com.abed.perfumeshop.customer.service.CustomerOrderService;
import com.abed.perfumeshop.notification.dto.response.EmailNotificationDTO;
import com.abed.perfumeshop.notification.dto.response.PushNotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.dto.request.CancelCustomerOrderRequest;
import com.abed.perfumeshop.order.dto.request.CreateCustomerOrderRequest;
import com.abed.perfumeshop.order.dto.response.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.OrderResponseDTO;
import com.abed.perfumeshop.order.dto.response.OrderSummaryDTO;
import com.abed.perfumeshop.order.entity.CustomerOrder;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.helper.OrderDetailBuilder;
import com.abed.perfumeshop.order.helper.OrderInventoryHelper;
import com.abed.perfumeshop.order.helper.OrderNumberGenerator;
import com.abed.perfumeshop.order.helper.OrderProcessingHelper;
import com.abed.perfumeshop.order.projection.OrderItemCount;
import com.abed.perfumeshop.order.repo.CustomerOrderRepo;
import com.abed.perfumeshop.order.repo.GuestOrderRepo;
import com.abed.perfumeshop.order.repo.OrderItemRepo;
import com.abed.perfumeshop.order.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private static final String CUSTOMER_ORDER_PREFIX = "CUS";

    private final CustomerOrderRepo customerOrderRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CouponRepo couponRepo;
    private final CouponUsageRepo couponUsageRepo;
    private final GuestOrderRepo guestOrderRepo;
    private final CustomerHelper customerHelper;
    private final OrderProcessingHelper orderProcessingHelper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderDetailBuilder orderDetailBuilder;
    private final OrderInventoryHelper orderInventoryHelper;
    private final NotificationSenderFacade notificationSenderFacade;
    private final MessageSource messageSource;
    private final EnumLocalizationService enumLocalizationService;

    @Value("${order.tracking.link}")
    private String orderTrackingLink;

    @Value("${notification.image.new-order}")
    private String newOrderImageUrl;

    @Value("${notification.image.cancelled}")
    private String cancelledImageUrl;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(CreateCustomerOrderRequest createCustomerOrderRequest) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        // Get and validate coupon if provided
        Coupon coupon = null;
        String couponCode = createCustomerOrderRequest.getCouponCode();

        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponRepo.findByCode(couponCode)
                    .orElseThrow(() -> new NotFoundException("coupon.not.found"));

            validateCouponBeforeUse(coupon, customer);
        }

        // Calculate shipping fee
        BigDecimal shippingFee = createCustomerOrderRequest.getGovernorate().getShippingFee();

        // Generate order number
        String orderNumber = orderNumberGenerator.generate(CUSTOMER_ORDER_PREFIX);

        // Create and save the order to get ID
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .notes(createCustomerOrderRequest.getNotes())
                .shippingFee(shippingFee)
                .build();
        orderRepo.save(order);

        // Process items and calculate subtotal
        BigDecimal subtotal = orderProcessingHelper.processOrderItems(order, createCustomerOrderRequest.getItems());

        // Calculate total before discount
        BigDecimal totalBeforeDiscount = subtotal.add(shippingFee);
        BigDecimal finalTotal = totalBeforeDiscount;

        // Apply discount if coupon exists
        if (coupon != null) {
            BigDecimal discountAmount = calculateDiscount(subtotal, coupon);
            finalTotal = totalBeforeDiscount.subtract(discountAmount).max(BigDecimal.ZERO);
        }

        // Set final total price (auto-saved by Hibernate at transaction commit)
        order.setTotalPrice(finalTotal);

        // Create and save customer order
        CustomerOrder customerOrder = CustomerOrder.builder()
                .governorate(createCustomerOrderRequest.getGovernorate())
                .address(createCustomerOrderRequest.getAddress())
                .phoneNumber(createCustomerOrderRequest.getPhoneNumber())
                .alternativePhoneNumber(createCustomerOrderRequest.getAlternativePhoneNumber())
                .order(order)
                .customer(customer)
                .coupon(coupon)
                .build();
        CustomerOrder saved = customerOrderRepo.save(customerOrder);

        // Record coupon usage after successful order
        if (coupon != null) {
            recordCouponUsage(coupon, customer, order);
        }

        // Get customer name
        String customerName = customer.getFirstName() + " " + customer.getLastName();

        // Send push notification to admin
        PushNotificationDTO pushNotificationDTO = PushNotificationDTO.builder()
                .targetUserType(UserType.ADMIN)
                .subject(messageSource.getMessage("notification.new.order.title", null, LocaleContextHolder.getLocale()))
                .body(messageSource.getMessage(
                        "notification.new.order.body",
                        new Object[]{customerName, order.getOrderNumber()},
                        LocaleContextHolder.getLocale()
                ))
                .order(order)
                .data(Map.of("orderNumber", order.getOrderNumber()))
                .imageUrl(newOrderImageUrl)
                .type(NotificationType.PUSH)
                .build();

        notificationSenderFacade.send(pushNotificationDTO);

        // Send email to customer
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customerName);
        templateVariables.put("orderNumber", order.getOrderNumber());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("trackingLink", orderTrackingLink + order.getOrderNumber());

        if (coupon != null) {
            templateVariables.put("couponApplied", true);
        }

        EmailNotificationDTO emailNotificationDTO = EmailNotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.order.confirmation.subject", null, LocaleContextHolder.getLocale()))
                .order(order)
                .coupon(coupon)
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/customer-order-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(emailNotificationDTO);

        return OrderResponseDTO.builder()
                .orderNumber(order.getOrderNumber())
                .build();
    }

    @Override
    public PageResponse<OrderSummaryDTO> getOrders(int page, int size, OrderStatus status) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        // Fetch and combine customer and guest orders
        List<Order> allOrders = Stream.concat(
                customerOrderRepo.findByCustomerAndStatusOrAll(customer, status).stream(),
                guestOrderRepo.findByClaimedCustomerAndStatusOrAll(customer, status).stream()
        ).toList();

        if (allOrders.isEmpty()) {
            return PageResponse.<OrderSummaryDTO>builder()
                    .content(Collections.emptyList())
                    .page(PageResponse.PageInfo.builder()
                            .size(size)
                            .number(page)
                            .totalElements(0)
                            .totalPages(0)
                            .build())
                    .build();
        }

        // Extract order IDs then Paginate orders
        List<Long> orderIds = allOrders.stream()
                .map(Order::getId)
                .toList();
        Page<Order> orderPage = orderRepo.findByOrderIdsIn(orderIds, PageRequest.of(page, size));

        // Fetch related data for current page
        List<String> pageOrderNumbers = orderPage.getContent().stream()
                .map(Order::getOrderNumber)
                .toList();

        Map<String, CustomerOrder> customerOrderMap = customerOrderRepo.findByOrder_OrderNumberIn(pageOrderNumbers).stream()
                .collect(Collectors.toMap(
                        co -> co.getOrder().getOrderNumber(),
                        co -> co
                ));

        Map<String, GuestOrder> guestOrderMap = guestOrderRepo.findByOrder_OrderNumberIn(pageOrderNumbers).stream()
                .collect(Collectors.toMap(
                        go -> go.getOrder().getOrderNumber(),
                        go -> go
                ));

        // Get item counts for each order
        Map<String, Integer> itemCounts = orderItemRepo.countItemsByOrders(orderPage.getContent()).stream()
                .collect(Collectors.toMap(
                        OrderItemCount::getOrderNumber,
                        count -> count.getItemCount().intValue()
                ));

        // Map orders to DTOs
        List<OrderSummaryDTO> pageContent = orderPage.getContent().stream()
                .map(order -> {
                    int itemCount = itemCounts.getOrDefault(order.getOrderNumber(), 0);
                    CustomerOrder customerOrder = customerOrderMap.get(order.getOrderNumber());

                    return customerOrder != null
                            ? mapCustomerOrderToDTO(customerOrder, itemCount)
                            : mapGuestOrderToDTO(guestOrderMap.get(order.getOrderNumber()), itemCount);
                })
                .collect(Collectors.toList());

        // Build paginated response
        return PageResponse.<OrderSummaryDTO>builder()
                .content(pageContent)
                .page(PageResponse.PageInfo.builder()
                        .size(orderPage.getSize())
                        .number(orderPage.getNumber())
                        .totalElements(orderPage.getTotalElements())
                        .totalPages(orderPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderDetailDTO getCustomerOrderByOrderNumber(String orderNumber) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        // Fetch customer order with security check
        CustomerOrder customerOrder = customerOrderRepo.findByCustomerAndOrder_OrderNumber(customer, orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        return orderDetailBuilder.buildCustomerOrderDetail(customerOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNumber, CancelCustomerOrderRequest cancelCustomerOrderRequest) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        // Fetch customer order with security check
        CustomerOrder customerOrder = customerOrderRepo.findByCustomerAndOrder_OrderNumber(customer, orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        Order order = customerOrder.getOrder();

        // Can only cancel PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("order.cannot.cancel.not.pending");
        }

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationSource(CancellationSource.CUSTOMER);
        order.setCancellationReason(
                cancelCustomerOrderRequest != null
                        ? cancelCustomerOrderRequest.getCancellationReason()
                        : null
        );
        order.setCancelledAt(LocalDateTime.now());

        // Restore inventory and reactivate if needed
        orderInventoryHelper.restoreInventory(orderNumber);

        // Get customer name
        String customerName = customer.getFirstName() + " " + customer.getLastName();

        // Send push notification to admin
        PushNotificationDTO pushNotificationDTO = PushNotificationDTO.builder()
                .targetUserType(UserType.ADMIN)
                .subject(messageSource.getMessage("notification.order.cancelled.title", null, LocaleContextHolder.getLocale()))
                .body(messageSource.getMessage(
                        "notification.order.cancelled.body",
                        new Object[]{customerName, order.getOrderNumber()},
                        LocaleContextHolder.getLocale()
                ))
                .order(order)
                .data(Map.of("orderNumber", order.getOrderNumber()))
                .imageUrl(cancelledImageUrl)
                .type(NotificationType.PUSH)
                .build();

        notificationSenderFacade.send(pushNotificationDTO);

        // Send email to customer
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customerName);
        templateVariables.put("orderNumber", order.getOrderNumber());
        templateVariables.put("newStatus", order.getStatus().name());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("cancelledAt", order.getCancelledAt());
        templateVariables.put("cancellationReason", order.getCancellationReason());
        templateVariables.put("deliveredAt", null);
        templateVariables.put("trackingLink", orderTrackingLink + order.getOrderNumber());

        EmailNotificationDTO emailNotificationDTO = EmailNotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.order.status.update.subject", null, LocaleContextHolder.getLocale()))
                .order(order)
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/order-status-update")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(emailNotificationDTO);
    }

    // ========== Private Helper Methods ==========
    private OrderSummaryDTO mapCustomerOrderToDTO(CustomerOrder customerOrder, int itemCount) {
        Order order = customerOrder.getOrder();
        Customer customer = customerOrder.getCustomer();

        return OrderSummaryDTO.builder()
                .orderNumber(order.getOrderNumber())
                .orderType(enumLocalizationService.getLocalizedName(OrderType.CUSTOMER))
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .orderDate(order.getOrderDate())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .totalPrice(order.getTotalPrice())
                .itemCount(itemCount)
                .guestEmail(null)
                .build();
    }

    private OrderSummaryDTO mapGuestOrderToDTO(GuestOrder guestOrder, int itemCount) {
        Order order = guestOrder.getOrder();

        return OrderSummaryDTO.builder()
                .orderNumber(order.getOrderNumber())
                .orderType(enumLocalizationService.getLocalizedName(OrderType.GUEST))
                .customerName(guestOrder.getUsername())
                .orderDate(order.getOrderDate())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .totalPrice(order.getTotalPrice())
                .itemCount(itemCount)
                .guestEmail(guestOrder.getEmail())
                .build();
    }

    private void validateCouponBeforeUse(Coupon coupon, Customer customer) {
        if (!coupon.getActive()) {
            throw new BadRequestException("coupon.inactive");
        }

        if (coupon.getMaxUsage() != null &&
                coupon.getUsageCount() >= coupon.getMaxUsage()) {
            throw new BadRequestException("coupon.max.usage.reached");
        }

        if (LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
            throw new BadRequestException("coupon.expired");
        }

        if (couponUsageRepo.existsByCouponAndCustomer(coupon, customer)) {
            throw new BadRequestException("coupon.already.used");
        }
    }

    private void recordCouponUsage(Coupon coupon, Customer customer, Order order) {
        CouponUsage couponUsage = CouponUsage.builder()
                .customer(customer)
                .coupon(coupon)
                .order(order)
                .build();
        couponUsageRepo.save(couponUsage);

        coupon.setUsageCount(coupon.getUsageCount() + 1);

        if (coupon.getMaxUsage() != null &&
                coupon.getUsageCount() >= coupon.getMaxUsage()) {
            coupon.setActive(false);
        }

        couponRepo.save(coupon);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, Coupon coupon) {
        return switch (coupon.getDiscountType()) {
            case PERCENTAGE -> subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> coupon.getDiscountValue().min(subtotal);
        };
    }

}
