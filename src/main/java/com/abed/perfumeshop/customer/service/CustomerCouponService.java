package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.coupon.dto.request.CouponValidationRequest;
import com.abed.perfumeshop.coupon.dto.response.CouponValidationResponse;

public interface CustomerCouponService {

    CouponValidationResponse validateCoupon(CouponValidationRequest couponValidationRequest);

}
