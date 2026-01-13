package com.abed.perfumeshop.order.helper;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.Item.repo.ItemRepo;
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
    private final CustomerOrderRepo customerOrderRepo;
    private final CouponUsageRepo couponUsageRepo;
    private final CouponRepo couponRepo;

    @Transactional
    public void restoreInventory(String orderNumber) {
        List<OrderItem> orderItems = orderItemRepo.findByOrder_OrderNumber(orderNumber);

        orderItems.forEach(orderItem -> {
            Item item = orderItem.getItem();

            // Add back the ordered quantity to inventory
            int newQuantity = item.getQuantity() + orderItem.getQuantity();
            item.setQuantity(newQuantity);

            // Auto-reactivate if item now has stock
            if (!item.getActive() && newQuantity > 0) {
                item.setActive(true);
            }

            itemRepo.save(item);
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

}
