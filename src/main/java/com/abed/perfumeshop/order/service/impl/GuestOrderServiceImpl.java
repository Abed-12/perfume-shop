package com.abed.perfumeshop.order.service.impl;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.entity.ItemPrice;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemRepo;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.OutOfStockException;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService {

    private final GuestOrderRepo guestOrderRepo;
    private final OrderRepo orderRepo;
    private final ItemRepo itemRepo;
    private final OrderItemRepo orderItemRepo;
    private final ItemPriceRepo itemPriceRepo;

    @Override
    @Transactional
    public Response<GuestOrderResponseDTO> createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest) {
        BigDecimal shippingFee = createGuestOrderRequest.getGovernorate().getShippingFee();
        BigDecimal subtotal = BigDecimal.ZERO;

        Order order = Order.builder()
                .notes(createGuestOrderRequest.getNotes())
                .shippingFee(shippingFee)
                .build();
        order = orderRepo.save(order);

        for (OrderItemRequest orderItemRequest : createGuestOrderRequest.getItems()){
            Item item = itemRepo.findById(orderItemRequest.getItemId())
                    .orElseThrow(() -> new NotFoundException("item.not.found"));

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

            ItemPrice itemPrice = itemPriceRepo.findCurrentActivePriceByItemId(item.getId())
                    .orElseThrow(() -> new NotFoundException("item.price.not.found"));

            item.setQuantity(item.getQuantity() - orderItemRequest.getQuantity());
            itemRepo.save(item);

            OrderItem orderItem = OrderItem.builder()
                    .quantity(orderItemRequest.getQuantity())
                    .unitPrice(itemPrice.getPrice())
                    .order(order)
                    .item(item)
                    .build();
            orderItemRepo.save(orderItem);

            BigDecimal itemSubtotal = itemPrice.getPrice()
                    .multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        order.setTotalPrice(subtotal.add(shippingFee));

        String trackingToken = UUID.randomUUID().toString();

        GuestOrder guestOrder = GuestOrder.builder()
                .username(createGuestOrderRequest.getUsername())
                .email(createGuestOrderRequest.getEmail())
                .phoneNumber(createGuestOrderRequest.getPhoneNumber())
                .alternativePhoneNumber(createGuestOrderRequest.getAlternativePhoneNumber())
                .governorate(createGuestOrderRequest.getGovernorate())
                .address(createGuestOrderRequest.getAddress())
                .trackingToken(trackingToken)
                .order(order)
                .build();
        guestOrderRepo.save(guestOrder);

        // Email service

        return Response.<GuestOrderResponseDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("order.created")
                .data(GuestOrderResponseDTO.builder()
                        .trackingToken(trackingToken)
                        .build())
                .build();
    }

}
