package com.abed.perfumeshop.coupon.repo;

import com.abed.perfumeshop.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepo extends JpaRepository<Coupon, Long> {

    Boolean existsByCode(String code);

    Optional<Coupon> findByActive(boolean active);

}
