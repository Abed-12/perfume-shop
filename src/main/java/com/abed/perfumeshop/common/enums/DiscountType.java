package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType implements LocalizableEnum {
    FIXED("discount.fixed"),
    PERCENTAGE("discount.percentage");

    private final String messageKey;
}
