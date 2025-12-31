package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OutOfStockException extends BaseException {

    public OutOfStockException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
