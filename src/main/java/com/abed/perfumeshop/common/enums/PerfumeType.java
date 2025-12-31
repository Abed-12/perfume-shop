package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PerfumeType implements LocalizableEnum {
    FEMALE("perfume.type.female"),
    MALE("perfume.type.male"),
    UNISEX("perfume.type.unisex");

    private final String messageKey;
}
