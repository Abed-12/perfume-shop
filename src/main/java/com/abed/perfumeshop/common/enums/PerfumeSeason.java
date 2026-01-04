package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PerfumeSeason implements LocalizableEnum{
    WINTER("perfume.season.winter"),
    SPRING("perfume.season.spring"),
    SUMMER("perfume.season.summer"),
    FALL("perfume.season.fall");

    private final String messageKey;
}
