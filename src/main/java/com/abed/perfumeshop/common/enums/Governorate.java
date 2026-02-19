package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Governorate {
    AMMAN(new BigDecimal("2.0")),
    ZARQA(new BigDecimal("3.0")),
    IRBID(new BigDecimal("4.0")),
    BALQA(new BigDecimal("4.0")),
    MADABA(new BigDecimal("4.0")),
    KARAK(new BigDecimal("5.0")),
    JERASH(new BigDecimal("5.0")),
    AJLOUN(new BigDecimal("5.0")),
    MAFRAQ(new BigDecimal("5.0")),
    TAFILAH(new BigDecimal("6.0")),
    MAAN(new BigDecimal("6.0")),
    AQABA(new BigDecimal("6.0"));

    private final BigDecimal shippingFee;
}
