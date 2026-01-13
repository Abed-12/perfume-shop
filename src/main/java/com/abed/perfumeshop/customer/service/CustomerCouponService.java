package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.coupon.dto.CouponValidationRequest;
import com.abed.perfumeshop.coupon.dto.CouponValidationResponse;

public interface CustomerCouponService {

    CouponValidationResponse validateCoupon(CouponValidationRequest couponValidationRequest);

}
