package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PerfumeSeason implements LocalizableEnum{
    WINTER("perfume.season.winter"),
    SUMMER("perfume.season.summer");

    private final String messageKey;
}

