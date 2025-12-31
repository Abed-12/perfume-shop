package com.abed.perfumeshop.common.service;

import com.abed.perfumeshop.common.enums.LocalizableEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnumLocalizationService {

    private final MessageSource messageSource;

    public <E extends Enum<E> & LocalizableEnum> String getLocalizedName(E enumValue) {
        if (enumValue == null) {
            return null;
        }
        return messageSource.getMessage(enumValue.getMessageKey(), null, LocaleContextHolder.getLocale());
    }

}