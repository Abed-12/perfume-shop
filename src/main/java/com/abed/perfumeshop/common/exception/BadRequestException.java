package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends BaseException {

    public BadRequestException(String messageKey){
        this(messageKey, null);
    }

    public BadRequestException(String messageKey, Object[] args){
        super(messageKey, args);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
