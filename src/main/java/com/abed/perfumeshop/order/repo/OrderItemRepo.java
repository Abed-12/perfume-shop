package com.abed.perfumeshop.order.repo;

import com.abed.perfumeshop.order.entity.Order;
import com.abed.perfumeshop.order.entity.OrderItem;
import com.abed.perfumeshop.order.projection.OrderItemCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderNumber(String orderNumber);

    @Query("SELECT oi.order.id AS orderId, COUNT(oi) AS itemCount FROM OrderItem oi " +
            "WHERE oi.order IN :orders " +
            "GROUP BY oi.order.id")
    List<OrderItemCount> countItemsByOrders(@Param("orders") List<Order> orders);

}
