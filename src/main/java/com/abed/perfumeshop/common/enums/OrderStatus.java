package com.abed.perfumeshop.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus implements LocalizableEnum {
    PENDING("order.status.pending"),
    PROCESSING("order.status.processing"),
    DELIVERED("order.status.delivered"),
    CANCELLED("order.status.cancelled");

    private final String messageKey;
}
