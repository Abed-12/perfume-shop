package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.coupon.dto.request.CouponRequest;
import com.abed.perfumeshop.coupon.dto.response.CouponResponse;
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
    public ResponseEntity<Response<Void>> createCoupon(@RequestBody @Valid CouponRequest couponRequest) {
        adminCouponService.createCoupon(couponRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Response.<Void>builder()
                                .statusCode(HttpStatus.CREATED.value())
                                .message("coupon.created.success")
                                .build()
                );
    }

    @GetMapping("/active")
    public ResponseEntity<Response<CouponResponse>> getActiveCoupon(){
        CouponResponse couponResponse = adminCouponService.getActiveCoupon();

        return ResponseEntity.ok(
                Response.<CouponResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("coupon.retrieved.success")
                        .data(couponResponse)
                        .build()
        );
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<Response<Void>> deactivateCoupon() {
        adminCouponService.deactivateCoupon();

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("coupon.deactivated.success")
                        .build()
        );
    }

}
