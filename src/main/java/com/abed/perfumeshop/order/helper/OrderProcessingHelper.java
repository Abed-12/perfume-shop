package com.abed.perfumeshop.order.helper;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.entity.ItemPrice;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemRepo;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.OutOfStockException;
import com.abed.perfumeshop.order.dto.OrderItemRequest;
import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.entity.OrderItem;
import com.abed.perfumeshop.order.repo.OrderItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderProcessingHelper {

    private final ItemRepo itemRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final OrderItemRepo orderItemRepo;

    public BigDecimal processOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : itemRequests){
            // Get and validate item
            Item item = itemRepo.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new NotFoundException("item.not.found"));

            validateItemAvailability(item, itemRequest.getQuantity());

            // Get current item price
            ItemPrice itemPrice = itemPriceRepo.findCurrentActivePriceByItemId(item.getId())
                    .orElseThrow(() -> new NotFoundException("item.price.not.found"));

            // Update item quantity
            updateItemQuantity(item, itemRequest.getQuantity());

            // Create and save order item
            OrderItem orderItem = OrderItem.builder()
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemPrice.getPrice())
                    .order(order)
                    .item(item)
                    .build();
            orderItemRepo.save(orderItem);

            // Add to subtotal
            BigDecimal itemTotal = itemPrice.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        return subtotal;
    }

    // ========== Private Helper Methods ==========
    private void validateItemAvailability(Item item, int requestedQuantity) {
        if (!item.getActive()) {
            throw new OutOfStockException("item.inactive", new Object[]{item.getName()});
        }

        if (item.getQuantity() < requestedQuantity){
            throw new OutOfStockException(
                    "item.insufficient.stock",
                    new Object[]{
                            item.getName(),
                            item.getQuantity(),
                            requestedQuantity
                    }
            );
        }
    }

    private void updateItemQuantity(Item item, int quantity) {
        int newQuantity = item.getQuantity() - quantity;
        item.setQuantity(newQuantity);

        if (newQuantity == 0) {
            item.setActive(false);
        }

        itemRepo.save(item);
    }

}
