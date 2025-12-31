package com.abed.perfumeshop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MaxImagesExceededException extends BaseException {

  public MaxImagesExceededException(String messageKey) {
    this(messageKey, null);
  }

  public MaxImagesExceededException(String messageKey, Object[] args) {
    super(messageKey, args);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

}
