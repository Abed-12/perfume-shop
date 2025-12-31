package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PerfumeSize implements LocalizableEnum {
    SIZE_50("perfume.size.50"),
    SIZE_100("perfume.size.100");

    private final String messageKey;
}

