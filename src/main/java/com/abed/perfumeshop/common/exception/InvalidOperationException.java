package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidOperationException extends BaseException {

    public InvalidOperationException(String messageKey) {
        this(messageKey, null);
    }

    public InvalidOperationException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
