package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String messageKey;
    private final Object[] args;

    protected BaseException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public abstract HttpStatus getHttpStatus();

}