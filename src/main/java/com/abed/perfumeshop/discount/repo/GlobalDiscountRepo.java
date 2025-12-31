package com.abed.perfumeshop.discount.repo;

import com.abed.perfumeshop.discount.entity.GlobalDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalDiscountRepo extends JpaRepository<GlobalDiscount, Long> {
}
