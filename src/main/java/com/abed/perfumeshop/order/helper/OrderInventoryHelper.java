package com.abed.perfumeshop.order.helper;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.entity.ItemPrice;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemRepo;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.coupon.repo.CouponRepo;
import com.abed.perfumeshop.coupon.repo.CouponUsageRepo;
import com.abed.perfumeshop.order.entity.CustomerOrder;
import com.abed.perfumeshop.order.entity.OrderItem;
import com.abed.perfumeshop.order.repo.CustomerOrderRepo;
import com.abed.perfumeshop.order.repo.OrderItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderInventoryHelper {

    private final OrderItemRepo orderItemRepo;
    private final ItemRepo itemRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final CustomerOrderRepo customerOrderRepo;
    private final CouponUsageRepo couponUsageRepo;
    private final CouponRepo couponRepo;

    @Transactional
    public void restoreInventory(String orderNumber) {
        List<OrderItem> orderItems = orderItemRepo.findByOrder_OrderNumber(orderNumber);

        orderItems.forEach(orderItem -> {
            Item item = orderItem.getItem();

            // Find the specific size that was ordered
            ItemPrice itemPrice = itemPriceRepo
                    .findFirstByItemIdAndPerfumeSizeOrderByEffectiveFromDesc(item.getId(), orderItem.getPerfumeSize())
                    .orElseThrow(() -> new NotFoundException("itemPrice.not.found"));

            // Add back the ordered quantity to inventory
            int newQuantity = itemPrice.getQuantity() + orderItem.getQuantity();
            itemPrice.setQuantity(newQuantity);

            // Auto-reactivate if item now has stock
            if (!itemPrice.getIsActive() && newQuantity > 0) {
                itemPrice.setIsActive(true);
            }

            itemPriceRepo.save(itemPrice);

            // Check if item should be reactivated
            checkAndReactivateItemIfNeeded(item);
        });

        // Remove coupon usage and decrease usage count
        customerOrderRepo.findByOrder_OrderNumber(orderNumber)
                .map(CustomerOrder::getCoupon)
                .ifPresent(coupon -> {
                    // Remove the coupon usage record
                    couponUsageRepo.deleteByOrder_OrderNumber(orderNumber);

                    // Decrease the coupon usage count
                    coupon.setUsageCount(Math.max(0, coupon.getUsageCount() - 1));

                    // Reactivate coupon if it was deactivated due to max usage
                    if (coupon.getMaxUsage() != null &&
                            coupon.getUsageCount() < coupon.getMaxUsage()) {
                        coupon.setActive(true);
                    }

                    couponRepo.save(coupon);
                });
    }

    // ========== Private Helper Methods ==========
    private void checkAndReactivateItemIfNeeded(Item item) {
        // Check if any size is now available
        boolean hasAvailableSize = itemPriceRepo.findByItemIdAndIsActiveTrue(item.getId())
                .stream()
                .anyMatch(ip -> ip.getQuantity() > 0);

        // Reactivate item if it was deactivated and now has available sizes
        if (!item.getActive() && hasAvailableSize) {
            item.setActive(true);
            itemRepo.save(item);
        }
    }

}
