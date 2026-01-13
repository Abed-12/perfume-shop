package com.abed.perfumeshop.customer.controller;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.coupon.dto.CouponValidationRequest;
import com.abed.perfumeshop.coupon.dto.CouponValidationResponse;
import com.abed.perfumeshop.customer.service.CustomerCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/coupons")
@RequiredArgsConstructor
public class CustomerCouponController {

    private final CustomerCouponService customerCouponService;

    @PostMapping("/validate")
    public ResponseEntity<Response<CouponValidationResponse>> validateCoupon(@RequestBody @Valid CouponValidationRequest couponValidationRequest){
        CouponValidationResponse couponValidationResponse = customerCouponService.validateCoupon(couponValidationRequest);

        return ResponseEntity.ok(
                Response.<CouponValidationResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("coupon.validated.successfully")
                        .data(couponValidationResponse)
                        .build()
        );
    }

}
