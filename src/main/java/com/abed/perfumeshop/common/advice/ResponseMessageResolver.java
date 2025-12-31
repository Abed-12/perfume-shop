package com.abed.perfumeshop.common.advice;

import com.abed.perfumeshop.common.res.Response;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@NullMarked
@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseMessageResolver implements ResponseBodyAdvice<Object> {

    private final MessageSource messageSource;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return Response.class.isAssignableFrom(
                returnType.nested().getNestedParameterType()
        );
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body,
                                            MethodParameter returnType,
                                            MediaType selectedContentType,
                                            Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            ServerHttpRequest request,
                                            ServerHttpResponse response) {

        if (body instanceof Response<?> responseBody) {
            String messageKey = responseBody.getMessage();

            if (messageKey != null && !messageKey.isEmpty()) {
                try {
                    String translatedMessage = messageSource.getMessage(
                            messageKey,
                            null,
                            LocaleContextHolder.getLocale()
                    );

                    return Response.builder()
                            .statusCode(responseBody.getStatusCode())
                            .message(translatedMessage)
                            .data(responseBody.getData())
                            .build();

                } catch (Exception e) {
                    return responseBody;
                }
            }
        }

        return body;
    }

}
