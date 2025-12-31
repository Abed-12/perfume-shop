package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.coupon.dto.CouponRequest;
import com.abed.perfumeshop.coupon.dto.CouponResponse;
import com.abed.perfumeshop.admin.service.AdminCouponService;
import com.abed.perfumeshop.common.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @PostMapping
    public ResponseEntity<Response<?>> createCoupon(@RequestBody @Valid CouponRequest couponRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCouponService.createCoupon(couponRequest));
    }

    @GetMapping("/active")
    public ResponseEntity<Response<CouponResponse>> getActiveCoupon(){
        return ResponseEntity.ok(adminCouponService.getActiveCoupon());
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<Response<?>> deactivateCoupon() {
        return ResponseEntity.ok(adminCouponService.deactivateCoupon());
    }

}
