package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends BaseException {

    public ValidationException(String messageKey) {
        this(messageKey, null);
    }

    public ValidationException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
