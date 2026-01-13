package com.abed.perfumeshop.order.repo;

import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.order.entity.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepo extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByCustomerAndOrder_OrderNumber(Customer customer, String orderNumber);

    Optional<CustomerOrder> findByOrder_OrderNumber(String orderNumber);

    @Query("SELECT co FROM CustomerOrder co " +
            "WHERE co.customer = :customer " +
            "AND (:status IS NULL OR co.order.status = :status)")
    List<CustomerOrder> findByCustomerAndStatusOrAll(@Param("customer") Customer customer, @Param("status") OrderStatus status);

    @Query("SELECT co FROM CustomerOrder co " +
            "WHERE (:status IS NULL OR co.order.status = :status) " +
            "ORDER BY co.order.orderDate DESC")
    Page<CustomerOrder> findByStatusOrAll(@Param("status") OrderStatus status, Pageable pageable);

}
