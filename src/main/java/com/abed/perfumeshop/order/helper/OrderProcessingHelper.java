package com.abed.perfumeshop.order.helper;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.entity.ItemPrice;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemRepo;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.OutOfStockException;
import com.abed.perfumeshop.order.dto.request.OrderItemRequest;
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

            // Get specific size item price
            ItemPrice itemPrice = itemPriceRepo.findByItemIdAndPerfumeSizeAndIsActiveTrue(
                    item.getId(),
                    itemRequest.getPerfumeSize()
            ).orElseThrow(() -> new NotFoundException("item.price.not.found"));

            // Validate availability
            validateItemAvailability(item, itemPrice, itemRequest.getQuantity());

            // Update item quantity
            updateItemQuantity(itemPrice, itemRequest.getQuantity());

            // Create and save order item
            OrderItem orderItem = OrderItem.builder()
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemPrice.getPrice())
                    .perfumeSize(itemRequest.getPerfumeSize())
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
    private void validateItemAvailability(Item item, ItemPrice itemPrice, int requestedQuantity) {
        // Check if item is active
        if (!item.getActive()) {
            throw new OutOfStockException("item.inactive", new Object[]{item.getName()});
        }

        if (itemPrice.getQuantity() < requestedQuantity){
            throw new OutOfStockException(
                    "item.insufficient.stock",
                    new Object[]{
                            item.getName(),
                            itemPrice.getPerfumeSize(),
                            itemPrice.getQuantity(),
                            requestedQuantity
                    }
            );
        }
    }

    private void updateItemQuantity(ItemPrice itemPrice, int quantity) {
        int newQuantity = itemPrice.getQuantity() - quantity;
        itemPrice.setQuantity(newQuantity);

        // Deactivate size if out of stock
        if (newQuantity == 0) {
            itemPrice.setIsActive(false);
        }

        itemPriceRepo.save(itemPrice);

        // Check if all sizes are out of stock
        checkAndDeactivateItemIfNeeded(itemPrice.getItem());
    }

    private void checkAndDeactivateItemIfNeeded(Item item) {
        // Check if any size is still available
        boolean hasAvailableSize = itemPriceRepo.findByItemIdAndIsActiveTrue(item.getId())
                .stream()
                .anyMatch(ip -> ip.getQuantity() > 0);

        // If no sizes available, deactivate the item
        if (!hasAvailableSize) {
            item.setActive(false);
            itemRepo.save(item);
        }
    }
}
