package com.abed.perfumeshop.common.exception;

import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // ============= Custom Business Exceptions =============
    @ExceptionHandler({
            AlreadyExistsException.class,
            BadRequestException.class,
            ImageProcessingException.class,
            InvalidOperationException.class,
            MaxImagesExceededException.class,
            NotFoundException.class,
            OutOfStockException.class,
            ValidationException.class
    })
    public ResponseEntity<Response<Void>> handleBusinessException(BaseException ex) {
        return buildResponse(ex.getHttpStatus(), ex.getMessageKey(), ex.getArgs());
    }

    // ============= Validation Exceptions =============
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return new ResponseEntity<>(
                Response.<Map<String, String>>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(messageSource.getMessage("validation.failed", null, LocaleContextHolder.getLocale()))
                        .data(errors)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    // ============= HTTP Protocol Exceptions =============
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleBodyMissing(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "request.data.invalid", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "media.type.not.supported", null);
    }

    // ============= Security Exceptions =============
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "exception.invalid.credentials", null);
    }

    // ============= Multipart/File Upload Exceptions =============
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Response<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return buildResponse(HttpStatus.CONTENT_TOO_LARGE, "file.size.limit.exceeded", null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Response<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "error.missing.part", new Object[]{ex.getRequestPartName()});
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Response<Void>> handleMultipartException(MultipartException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "error.multipart.invalid", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response<Void>> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "request.parameter.required", new Object[]{ex.getParameterName()});
    }

    // ============= Catch-All Exception =============
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleAllUnknownException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal.server", null);
    }

    // ============= Private Helper Methods =============
    private ResponseEntity<Response<Void>> buildResponse(HttpStatus status, String messageKey, Object[] args) {
        return ResponseEntity.status(status)
                .body(Response.<Void>builder()
                    .statusCode(status.value())
                    .message(messageSource.getMessage(
                            messageKey,
                            args,
                            LocaleContextHolder.getLocale())
                    )
                    .build()
                );
    }

}

