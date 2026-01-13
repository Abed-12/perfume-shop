package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType implements LocalizableEnum{
    CUSTOMER("customer"),
    GUEST("guest");

    private final String messageKey;
}
