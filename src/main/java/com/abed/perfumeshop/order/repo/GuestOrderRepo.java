package com.abed.perfumeshop.order.repo;

import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.common.projection.EmailRecipientProjection;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestOrderRepo extends JpaRepository<GuestOrder, Long> {

    List<GuestOrder> findByEmailAndClaimedByCustomerIsNull(String email);

    Optional<GuestOrder> findByEmailAndOrder_OrderNumber(String email, String orderNumber);

    Optional<GuestOrder> findByOrder_OrderNumber(String orderNumber);

    @Query("SELECT g.email AS email, " +
            "MAX(g.username) AS name, " +
            "false AS isCustomer " +
            "FROM GuestOrder g " +
            "GROUP BY g.email")
    List<EmailRecipientProjection> findAllDistinctEmailProjections();

    @Query("SELECT go.order FROM GuestOrder go " +
            "WHERE go.claimedByCustomer = :customer " +
            "AND (:status IS NULL OR go.order.status = :status)")
    List<Order> findByClaimedCustomerAndStatusOrAll(@Param("customer") Customer customer, @Param("status") OrderStatus status);

    @Query("SELECT go FROM GuestOrder go " +
            "WHERE go.order.orderNumber IN :orderNumbers")
    List<GuestOrder> findByOrder_OrderNumberIn(@Param("orderNumbers") List<String> orderNumbers);

    @Query("SELECT go FROM GuestOrder go " +
            "WHERE (:status IS NULL OR go.order.status = :status) " +
            "ORDER BY go.order.orderDate DESC")
    Page<GuestOrder> findByStatusOrAll(@Param("status") OrderStatus status, Pageable pageable);

}
