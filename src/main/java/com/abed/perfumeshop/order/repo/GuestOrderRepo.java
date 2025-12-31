package com.abed.perfumeshop.order.repo;

import com.abed.perfumeshop.order.entity.GuestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestOrderRepo extends JpaRepository<GuestOrder, Long> {
}
