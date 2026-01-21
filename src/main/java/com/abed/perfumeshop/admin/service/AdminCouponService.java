package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.coupon.dto.request.CouponRequest;
import com.abed.perfumeshop.coupon.dto.response.CouponResponse;

public interface AdminCouponService {

    void createCoupon(CouponRequest couponRequest);

    CouponResponse getActiveCoupon();

    void deactivateCoupon();

}
