package com.abed.perfumeshop.customer.service.impl;

import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.coupon.dto.request.CouponValidationRequest;
import com.abed.perfumeshop.coupon.dto.response.CouponValidationResponse;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.coupon.repo.CouponRepo;
import com.abed.perfumeshop.coupon.repo.CouponUsageRepo;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.helper.CustomerHelper;
import com.abed.perfumeshop.customer.service.CustomerCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerCouponServiceImpl implements CustomerCouponService {

    private final CouponRepo couponRepo;
    private final CouponUsageRepo couponUsageRepo;
    private final CustomerHelper customerHelper;
    private final EnumLocalizationService enumLocalizationService;

    @Override
    public CouponValidationResponse validateCoupon(CouponValidationRequest couponValidationRequest) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        // Find coupon by code
        Coupon coupon = couponRepo.findByCode(couponValidationRequest.getCouponCode())
                .orElseThrow(() -> new NotFoundException("coupon.not.found"));

        // Check if active
        if (!coupon.getActive()) {
            throw new BadRequestException("coupon.inactive");
        }

        // Check expiry date
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new BadRequestException("coupon.expired");
        }

        // Check global usage limit
        if (coupon.getMaxUsage() != null &&
                coupon.getUsageCount() >= coupon.getMaxUsage()) {
            throw new BadRequestException("coupon.max.usage.reached");
        }

        // Check if customer already used this coupon
        if (couponUsageRepo.existsByCouponAndCustomer(coupon, customer)) {
            throw new BadRequestException("coupon.already.used");
        }

        // Calculate discount and final price
        BigDecimal discountAmount = calculateDiscount(couponValidationRequest.getOrderTotal(), coupon);
        BigDecimal finalPrice = couponValidationRequest.getOrderTotal()
                .subtract(discountAmount)
                .max(BigDecimal.ZERO);

        // Build response
        return CouponValidationResponse.builder()
                .couponCode(coupon.getCode())
                .discountType(enumLocalizationService.getLocalizedName(coupon.getDiscountType()))
                .discountValue(coupon.getDiscountValue())
                .originalPrice(couponValidationRequest.getOrderTotal())
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .build();
    }

    // ========== Private Helper Methods ==========
    private BigDecimal calculateDiscount(BigDecimal orderTotal, Coupon coupon) {
        return switch (coupon.getDiscountType()) {
            case PERCENTAGE -> orderTotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> coupon.getDiscountValue().min(orderTotal);
        };
    }

}
