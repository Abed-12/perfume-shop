package com.abed.perfumeshop.order.service.impl;

import com.abed.perfumeshop.common.enums.CancellationSource;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.ValidationException;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.notification.dto.response.EmailNotificationDTO;
import com.abed.perfumeshop.notification.dto.response.PushNotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.dto.request.CancelGuestOrderRequest;
import com.abed.perfumeshop.order.dto.request.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.response.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.OrderResponseDTO;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.helper.OrderDetailBuilder;
import com.abed.perfumeshop.order.helper.OrderInventoryHelper;
import com.abed.perfumeshop.order.helper.OrderNumberGenerator;
import com.abed.perfumeshop.order.helper.OrderProcessingHelper;
import com.abed.perfumeshop.order.repo.GuestOrderRepo;
import com.abed.perfumeshop.order.repo.OrderRepo;
import com.abed.perfumeshop.order.service.GuestOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService {

    private static final String GUEST_ORDER_PREFIX = "GST";

    private final GuestOrderRepo guestOrderRepo;
    private final OrderRepo orderRepo;
    private final CustomerRepo customerRepo;
    private final OrderProcessingHelper orderProcessingHelper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderDetailBuilder orderDetailBuilder;
    private final OrderInventoryHelper orderInventoryHelper;
    private final NotificationSenderFacade notificationSenderFacade;
    private final MessageSource messageSource;

    @Value("${order.tracking.link}")
    private String orderTrackingLink;

    @Value("${notification.image.new-order}")
    private String newOrderImageUrl;

    @Value("${notification.image.cancelled}")
    private String cancelledImageUrl;

    @Override
    @Transactional
    public OrderResponseDTO createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest) {
        // Calculate shipping fee
        BigDecimal shippingFee = createGuestOrderRequest.getGovernorate().getShippingFee();

        // Generate unique order number
        String orderNumber = orderNumberGenerator.generate(GUEST_ORDER_PREFIX);

        // Create and save the order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .notes(createGuestOrderRequest.getNotes())
                .shippingFee(shippingFee)
                .build();
        order = orderRepo.save(order);

        // Process items and calculate subtotal
        BigDecimal subtotal = orderProcessingHelper.processOrderItems(order, createGuestOrderRequest.getItems());

        // Set final total price (auto-saved by Hibernate at transaction commit)
        order.setTotalPrice(subtotal.add(shippingFee));


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
                .claimedByCustomer(existingCustomer.orElse(null))
                .claimedAt(existingCustomer.isPresent() ? LocalDateTime.now() : null)
                .order(order)
                .build();
        guestOrderRepo.save(guestOrder);

        // Send push notification to admin
        PushNotificationDTO pushNotificationDTO = PushNotificationDTO.builder()
                .targetUserType(UserType.ADMIN)
                .subject(messageSource.getMessage("notification.new.order.title", null, LocaleContextHolder.getLocale()))
                .body(messageSource.getMessage(
                        "notification.new.order.body",
                        new Object[]{createGuestOrderRequest.getUsername(), order.getOrderNumber()},
                        LocaleContextHolder.getLocale()
                ))
                .order(order)
                .data(Map.of(
                        "orderNumber", order.getOrderNumber(),
                        "email", createGuestOrderRequest.getEmail()
                ))
                .imageUrl(newOrderImageUrl)
                .type(NotificationType.PUSH)
                .build();

        notificationSenderFacade.send(pushNotificationDTO);

        // Send email to guest
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", createGuestOrderRequest.getUsername());
        templateVariables.put("orderNumber", order.getOrderNumber());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("trackingLink",
                orderTrackingLink + order.getOrderNumber() + "&email=" + guestOrder.getEmail());

        EmailNotificationDTO emailNotificationDTO = EmailNotificationDTO.builder()
                .recipient(createGuestOrderRequest.getEmail())
                .subject(messageSource.getMessage("notification.order.confirmation.subject", null, LocaleContextHolder.getLocale()))
                .order(order)
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/guest-order-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(emailNotificationDTO);

        return OrderResponseDTO.builder()
                .orderNumber(order.getOrderNumber())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GuestOrderDetailDTO getGuestOrderByEmailAndOrderNumber(String email, String orderNumber) {
        // Validate input parameters
        if (email == null || email.isBlank()) {
            throw new ValidationException("email.required");
        }

        if (orderNumber == null || orderNumber.isBlank()) {
            throw new ValidationException("order.number.required");
        }

        // Fetch guest order by tracking token
        GuestOrder guestOrder = guestOrderRepo.findByEmailAndOrder_OrderNumber(email, orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        return orderDetailBuilder.buildGuestOrderDetail(guestOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNumber, CancelGuestOrderRequest cancelGuestOrderRequest) {
        // Verify email + orderNumber for security
        GuestOrder guestOrder = guestOrderRepo.findByEmailAndOrder_OrderNumber(cancelGuestOrderRequest.getEmail(), orderNumber)
                .orElseThrow(() -> new NotFoundException("order.not.found"));

        Order order = guestOrder.getOrder();

        // Can only cancel PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("order.cannot.cancel.not.pending");
        }

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationSource(CancellationSource.CUSTOMER);
        order.setCancellationReason(cancelGuestOrderRequest.getCancellationReason());
        order.setCancelledAt(LocalDateTime.now());

        // Restore inventory and reactivate if needed
        orderInventoryHelper.restoreInventory(orderNumber);

        // Send push notification to admin
        PushNotificationDTO pushNotificationDTO = PushNotificationDTO.builder()
                .targetUserType(UserType.ADMIN)
                .subject(messageSource.getMessage("notification.order.cancelled.title", null, LocaleContextHolder.getLocale()))
                .body(messageSource.getMessage(
                        "notification.order.cancelled.body",
                        new Object[]{guestOrder.getUsername(), order.getOrderNumber()},
                        LocaleContextHolder.getLocale()
                ))
                .order(order)
                .data(Map.of(
                        "orderNumber", order.getOrderNumber(),
                        "email", guestOrder.getEmail()
                ))
                .imageUrl(cancelledImageUrl)
                .type(NotificationType.PUSH)
                .build();

        notificationSenderFacade.send(pushNotificationDTO);

        // Send email to guest
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", guestOrder.getUsername());
        templateVariables.put("orderNumber", order.getOrderNumber());
        templateVariables.put("newStatus", order.getStatus().name());
        templateVariables.put("totalPrice", String.format("%.2f", order.getTotalPrice()));
        templateVariables.put("orderDate", order.getOrderDate());
        templateVariables.put("cancelledAt", order.getCancelledAt());
        templateVariables.put("cancellationReason", order.getCancellationReason());
        templateVariables.put("deliveredAt", null);
        templateVariables.put("trackingLink",
                orderTrackingLink + order.getOrderNumber() + "&email=" + guestOrder.getEmail());

        EmailNotificationDTO emailNotificationDTO = EmailNotificationDTO.builder()
                .recipient(guestOrder.getEmail())
                .subject(messageSource.getMessage("notification.order.status.update.subject", null, LocaleContextHolder.getLocale()))
                .order(order)
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/order-status-update")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(emailNotificationDTO);
    }

}
