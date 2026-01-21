package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.notification.service.CouponNotificationService;
import com.abed.perfumeshop.passwordResetCode.service.CodeGenerator;
import com.abed.perfumeshop.coupon.dto.request.CouponRequest;
import com.abed.perfumeshop.coupon.dto.response.CouponResponse;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.coupon.repo.CouponRepo;
import com.abed.perfumeshop.admin.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {

    private final CouponRepo couponRepo;
    private final AdminHelper adminHelper;
    private final CouponNotificationService couponNotificationService;
    private final CodeGenerator codeGenerator;
    private final EnumLocalizationService enumLocalizationService;

    @Override
    public void createCoupon(CouponRequest couponRequest) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        couponRepo.findByActiveTrue()
                .ifPresent(c -> {
                    throw new AlreadyExistsException("coupon.active.already.exists");
                });

        String code = codeGenerator.generateUniqueCode(couponRepo::existsByCode);

        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(couponRequest.getDiscountType())
                .discountValue(couponRequest.getDiscountValue())
                .expiryDate(couponRequest.getExpiryDate())
                .maxUsage(couponRequest.getMaxUsage())
                .createdBy(admin)
                .build();

        couponRepo.save(coupon);

        couponNotificationService.sendCouponToAllUsers(coupon, LocaleContextHolder.getLocale());
    }

    @Override
    public CouponResponse getActiveCoupon() {
        adminHelper.getCurrentLoggedInUser();

        Coupon coupon = couponRepo.findByActiveTrue()
                .orElseThrow(() -> new NotFoundException("coupon.not.found.active"));

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(enumLocalizationService.getLocalizedName(coupon.getDiscountType()))
                .discountValue(coupon.getDiscountValue())
                .expiryDate(coupon.getExpiryDate())
                .active(coupon.getActive())
                .usageCount(coupon.getUsageCount())
                .maxUsage(coupon.getMaxUsage())
                .build();
    }

    @Override
    public void deactivateCoupon() {
        adminHelper.getCurrentLoggedInUser();

        Coupon coupon = couponRepo.findByActiveTrue()
                .orElseThrow(() -> new NotFoundException("coupon.not.found.active"));

        coupon.setActive(false);
        couponRepo.save(coupon);
    }

}
