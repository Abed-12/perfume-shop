package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminOrderService;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.CancellationSource;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.ValidationException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.notification.dto.response.EmailNotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.dto.response.AdminOrderSummaryDTO;
import com.abed.perfumeshop.order.dto.response.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.request.UpdateOrderStatusRequest;
import com.abed.perfumeshop.order.entity.CustomerOrder;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.helper.OrderDetailBuilder;
import com.abed.perfumeshop.order.helper.OrderInventoryHelper;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepo orderRepo;
    private final CustomerOrderRepo customerOrderRepo;
    private final GuestOrderRepo guestOrderRepo;
    private final OrderItemRepo orderItemRepo;
    private final AdminHelper adminHelper;
    private final OrderDetailBuilder orderDetailBuilder;
    private final OrderInventoryHelper orderInventoryHelper;
    private final EnumLocalizationService enumLocalizationService;
    private final NotificationSenderFacade notificationSenderFacade;
    private final MessageSource messageSource;

    @Value("${order.tracking.link}")
    private String orderTrackingLink;

    @Override
    public PageResponse<AdminOrderSummaryDTO> getCustomerOrders(int page, int size, OrderStatus status) {
        adminHelper.getCurrentLoggedInUser();

        // Fetch customer orders
        Page<CustomerOrder> ordersPage = customerOrderRepo.findByStatusOrAll(status, PageRequest.of(page, size));

        // Extract orders for item count query
        List<Order> orders = ordersPage.getContent().stream()
                .map(CustomerOrder::getOrder)
                .toList();

        // Get item counts
        Map<String, Integer> itemCounts = getItemCounts(orders);

        // Map to DTOs
        List<AdminOrderSummaryDTO> orderDTOs = ordersPage.getContent().stream()
                .map(co -> mapCustomerOrderToDTO(co, itemCounts))
                .toList();

        return PageResponse.<AdminOrderSummaryDTO>builder()
                .content(orderDTOs)
                .page(PageResponse.PageInfo.builder()
                        .size(ordersPage.getSize())
                        .number(ordersPage.getNumber())
                        .totalElements(ordersPage.getTotalElements())
                        .totalPages(ordersPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public PageResponse<AdminOrderSummaryDTO> getGuestOrders(int page, int size, OrderStatus status) {
        adminHelper.getCurrentLoggedInUser();

        // Fetch guest orders
        Page<GuestOrder> ordersPage = guestOrderRepo.findByStatusOrAll(status, PageRequest.of(page, size));

        // Extract orders for item count query
        List<Order> orders = ordersPage.getContent().stream()
                .map(GuestOrder::getOrder)
                .toList();

        // Get item counts
        Map<String, Integer> itemCounts = getItemCounts(orders);

        // Map to DTOs
        List<AdminOrderSummaryDTO> orderDTOs = ordersPage.getContent().stream()
                .map(go -> mapGuestOrderToDTO(go, itemCounts))
                .toList();

        return PageResponse.<AdminOrderSummaryDTO>builder()
                .content(orderDTOs)
                .page(PageResponse.PageInfo.builder()
                        .size(ordersPage.getSize())
                        .number(ordersPage.getNumber())
                        .totalElements(ordersPage.getTotalElements())
                        .totalPages(ordersPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public CustomerOrderDetailDTO getCustomerOrderByOrderNumber(String orderNumber) {
        adminHelper.getCurrentLoggedInUser();

        // Fetch order without customer check
        CustomerOrder customerOrder = customerOrderRepo.findByOrder_OrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        return orderDetailBuilder.buildCustomerOrderDetail(customerOrder);
    }

    @Override
    public GuestOrderDetailDTO getGuestOrderByEmailAndOrderNumber(String email, String orderNumber) {
        adminHelper.getCurrentLoggedInUser();

        // Fetch guest order
        GuestOrder guestOrder = guestOrderRepo
                .findByEmailAndOrder_OrderNumber(email, orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        return orderDetailBuilder.buildGuestOrderDetail(guestOrder);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderNumber, UpdateOrderStatusRequest updateOrderStatusRequest) {
        adminHelper.getCurrentLoggedInUser();

        // Fetch order
        Order order = orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = updateOrderStatusRequest.getStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus, updateOrderStatusRequest.getCancellationReason());

        // Restore inventory if cancelling
        if (newStatus == OrderStatus.CANCELLED) {
            orderInventoryHelper.restoreInventory(orderNumber);
        }

        // Update status
        order.setStatus(newStatus);

        // Update timestamps
        updateStatusTimestamps(order, newStatus, updateOrderStatusRequest.getCancellationReason());

        // Send email to customer or guest
        Optional<CustomerOrder> customerOrder = customerOrderRepo.findByOrder_OrderNumber(order.getOrderNumber());
        if (customerOrder.isPresent()) {
            Customer customer = customerOrder.get().getCustomer();
            String recipientEmail = customer.getEmail();
            String customerName = customer.getFirstName() + " " + customer.getLastName();
            sendStatusUpdateEmail(order, recipientEmail, customerName, newStatus,
                    updateOrderStatusRequest.getCancellationReason(), false);
        } else {
            Optional<GuestOrder> guestOrder = guestOrderRepo.findByOrder_OrderNumber(order.getOrderNumber());
            if (guestOrder.isPresent()) {
                String recipientEmail = guestOrder.get().getEmail();
                String customerName = guestOrder.get().getUsername();
                sendStatusUpdateEmail(order, recipientEmail, customerName, newStatus,
                        updateOrderStatusRequest.getCancellationReason(), true);
            }
        }
    }

    // ========== Private Helper Methods ==========
    private Map<String, Integer> getItemCounts(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }

        return orderItemRepo.countItemsByOrders(orders).stream()
                .collect(Collectors.toMap(
                        OrderItemCount::getOrderNumber,
                        count -> count.getItemCount().intValue()
                ));
    }

    private AdminOrderSummaryDTO mapCustomerOrderToDTO(
            CustomerOrder customerOrder,
            Map<String, Integer> itemCounts
    ) {
        Order order = customerOrder.getOrder();
        Customer customer = customerOrder.getCustomer();

        return AdminOrderSummaryDTO.builder()
                .orderNumber(order.getOrderNumber())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .orderDate(order.getOrderDate())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .customerEmail(customer.getEmail())
                .totalPrice(order.getTotalPrice())
                .itemCount(itemCounts.getOrDefault(order.getOrderNumber(), 0))
                .build();
    }

    private AdminOrderSummaryDTO mapGuestOrderToDTO(
            GuestOrder guestOrder,
            Map<String, Integer> itemCounts
    ) {
        Order order = guestOrder.getOrder();

        return AdminOrderSummaryDTO.builder()
                .orderNumber(order.getOrderNumber())
                .status(enumLocalizationService.getLocalizedName(order.getStatus()))
                .orderDate(order.getOrderDate())
                .customerName(guestOrder.getUsername())
                .customerEmail(guestOrder.getEmail())
                .totalPrice(order.getTotalPrice())
                .itemCount(itemCounts.getOrDefault(order.getOrderNumber(), 0))
                .build();
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus, String cancellationReason) {
        // Validate same status
        if (currentStatus == newStatus) {
            throw new BadRequestException("order.status.same");
        }

        // Validate cancellation reason
        if (newStatus == OrderStatus.CANCELLED) {
            if (cancellationReason == null || cancellationReason.isBlank()) {
                throw new ValidationException("order.cancellation.reason.required");
            }
        }

        // Validate transition rules
        switch (currentStatus) {
            case PENDING -> {
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("order.status.transition.not.allowed");
                }
            }
            case PROCESSING -> {
                if (newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELLED) {
                    throw new BadRequestException("order.status.transition.not.allowed");
                }
            }
            case DELIVERED, CANCELLED ->
                throw new BadRequestException("order.status.cannot.be.changed");

        }
    }

    private void updateStatusTimestamps(Order order, OrderStatus newStatus, String cancellationReason) {
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancellationSource(CancellationSource.ADMIN);
            order.setCancellationReason(cancellationReason);
            order.setCancelledAt(LocalDateTime.now());
        }
    }

    private void sendStatusUpdateEmail(Order order, String recipientEmail, String customerName,
                                       OrderStatus newStatus, String cancellationReason, boolean isGuestOrder) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customerName);
        templateVariables.put("orderNumber", order.getOrderNumber());
        templateVariables.put("newStatus", newStatus.name());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("cancelledAt", order.getCancelledAt());
        templateVariables.put("cancellationReason", cancellationReason);
        templateVariables.put("deliveredAt", order.getDeliveredAt());

        String trackingLink = isGuestOrder
                ? orderTrackingLink + order.getOrderNumber() + "&email=" + recipientEmail
                : orderTrackingLink + order.getOrderNumber();

        templateVariables.put("trackingLink", trackingLink);

        EmailNotificationDTO emailNotificationDTO = EmailNotificationDTO.builder()
                .recipient(recipientEmail)
                .subject(messageSource.getMessage("notification.order.status.update.subject",
                        null,
                        LocaleContextHolder.getLocale()))
                .order(order)
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/order-status-update")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(emailNotificationDTO);
    }

}
