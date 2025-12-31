package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Governorate implements LocalizableEnum {
    AMMAN("governorate.amman"),
    IRBID("governorate.irbid"),
    ZARQA("governorate.zarqa"),
    BALQA("governorate.balqa"),
    MADABA("governorate.madaba"),
    KARAK("governorate.karak"),
    TAFILAH("governorate.tafilah"),
    MAAN("governorate.maan"),
    AQABA("governorate.aqaba"),
    JERASH("governorate.jerash"),
    AJLOUN("governorate.ajloun"),
    MAFRAQ("governorate.mafraq");

    private final String messageKey;
}
