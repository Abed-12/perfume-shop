package com.abed.perfumeshop.order.repo;

import com.abed.perfumeshop.order.entity.AuthenticatedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticatedOrderRepo extends JpaRepository<AuthenticatedOrder, Long> {
}
