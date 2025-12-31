package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.passwordResetCode.service.CodeGenerator;
import com.abed.perfumeshop.coupon.dto.CouponRequest;
import com.abed.perfumeshop.coupon.dto.CouponResponse;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.coupon.repo.CouponRepo;
import com.abed.perfumeshop.admin.service.AdminCouponService;
import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCouponServiceImpl implements AdminCouponService {

    private final CouponRepo couponRepo;
    private final AdminHelper adminHelper;
    private final CodeGenerator codeGenerator;
    private final EnumLocalizationService enumLocalizationService;

    @Override
    public Response<?> createCoupon(CouponRequest couponRequest) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        couponRepo.findByActive(true)
                .ifPresent(c -> {
                    throw new IllegalStateException("coupon.active.already.exists");
                });

        String code = codeGenerator.generateUniqueCode(couponRepo::existsByCode);

        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(couponRequest.getDiscountType())
                .discountValue(couponRequest.getDiscountValue())
                .expiryDate(couponRequest.getExpiryDate())
                .createdBy(admin)
                .build();

        couponRepo.save(coupon);

        // Send email or push


        return Response.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("coupon.created.success")
                .build();
    }

    @Override
    public Response<CouponResponse> getActiveCoupon() {
        adminHelper.getCurrentLoggedInUser();

        Coupon coupon = couponRepo.findByActive(true)
                .orElseThrow(() -> new NotFoundException("coupon.not.found.active"));

        CouponResponse couponResponse = CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(enumLocalizationService.getLocalizedName(coupon.getDiscountType()))
                .discountValue(coupon.getDiscountValue())
                .expiryDate(coupon.getExpiryDate())
                .active(coupon.getActive())
                .usageCount(coupon.getUsageCount())
                .maxUsage(coupon.getMaxUsage())
                .build();

        return Response.<CouponResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("coupon.retrieved.success")
                .data(couponResponse)
                .build();
    }

    @Override
    public Response<?> deactivateCoupon() {
        adminHelper.getCurrentLoggedInUser();

        Coupon coupon = couponRepo.findByActive(true)
                .orElseThrow(() -> new NotFoundException("coupon.not.found.active"));

        coupon.setActive(false);
        couponRepo.save(coupon);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("coupon.deactivated.success")
                .build();
    }

}
