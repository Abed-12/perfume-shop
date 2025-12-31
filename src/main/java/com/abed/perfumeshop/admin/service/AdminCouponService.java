package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.coupon.dto.CouponRequest;
import com.abed.perfumeshop.coupon.dto.CouponResponse;
import com.abed.perfumeshop.common.res.Response;

public interface AdminCouponService {

    Response<?> createCoupon(CouponRequest couponRequest);

    Response<?> deactivateCoupon();

    Response<CouponResponse> getActiveCoupon();

}
