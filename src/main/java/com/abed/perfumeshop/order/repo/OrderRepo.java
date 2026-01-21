package com.abed.perfumeshop.order.repo;

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
public interface OrderRepo extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o " +
            "WHERE o.id IN :orderIds " +
            "ORDER BY o.orderDate DESC")
    Page<Order> findByOrderIdsIn(@Param("orderIds") List<Long> orderIds, Pageable pageable);

}
