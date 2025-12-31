package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AlreadyExistsException extends BaseException {

    public AlreadyExistsException(String messageKey) {
        this(messageKey, null);
    }

    public AlreadyExistsException(String messageKey, Object[] args) {
        super(messageKey, args);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

}
