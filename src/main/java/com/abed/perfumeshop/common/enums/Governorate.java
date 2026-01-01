package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Governorate implements LocalizableEnum {
    AMMAN("governorate.amman", new BigDecimal("2.0")),
    ZARQA("governorate.zarqa", new BigDecimal("3.0")),
    IRBID("governorate.irbid", new BigDecimal("4.0")),
    BALQA("governorate.balqa", new BigDecimal("4.0")),
    MADABA("governorate.madaba", new BigDecimal("4.0")),
    KARAK("governorate.karak", new BigDecimal("5.0")),
    JERASH("governorate.jerash", new BigDecimal("5.0")),
    AJLOUN("governorate.ajloun",new BigDecimal("5.0")),
    MAFRAQ("governorate.mafraq", new BigDecimal("5.0")),
    TAFILAH("governorate.tafilah", new BigDecimal("6.0")),
    MAAN("governorate.maan", new BigDecimal("6.0")),
    AQABA("governorate.aqaba", new BigDecimal("6.0"));

    private final String messageKey;
    private final BigDecimal shippingFee;
}
