package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.coupon.dto.CouponRequest;
import com.abed.perfumeshop.coupon.dto.CouponResponse;

public interface AdminCouponService {

    void createCoupon(CouponRequest couponRequest);

    CouponResponse getActiveCoupon();

    void deactivateCoupon();

}
