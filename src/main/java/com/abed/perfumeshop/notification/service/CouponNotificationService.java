package com.abed.perfumeshop.notification.service;

import com.abed.perfumeshop.coupon.entity.Coupon;

import java.util.Locale;

public interface CouponNotificationService  {

    void sendCouponToAllUsers(Coupon coupon, Locale locale);

}
