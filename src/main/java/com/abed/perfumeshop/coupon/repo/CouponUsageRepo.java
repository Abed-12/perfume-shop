package com.abed.perfumeshop.coupon.repo;

import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.coupon.entity.CouponUsage;
import com.abed.perfumeshop.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepo extends JpaRepository<CouponUsage, Long> {

    boolean existsByCouponAndCustomer(Coupon coupon, Customer customer);

    void deleteByOrder_OrderNumber(String orderNumber);

}
