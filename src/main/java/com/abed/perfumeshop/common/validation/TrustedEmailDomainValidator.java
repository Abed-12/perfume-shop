package com.abed.perfumeshop.common.validation;

import com.abed.perfumeshop.config.app.EmailValidationConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

@RequiredArgsConstructor
public class TrustedEmailDomainValidator implements ConstraintValidator<TrustedEmailDomain, String> {

    private final EmailValidationConfig emailConfig;
    private String messageTemplate;

    @Override
    public void initialize(TrustedEmailDomain constraintAnnotation) {
        this.messageTemplate = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        if (!email.contains("@")) {
            return false;
        }

        String domain = extractDomain(email).toLowerCase().trim();
        boolean isValid = emailConfig.getTrustedDomains().contains(domain);

        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();

            String allowedDomains = String.join(", ", emailConfig.getTrustedDomains());

            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                    .addMessageParameter("0", allowedDomains)
                    .buildConstraintViolationWithTemplate(messageTemplate)
                    .addConstraintViolation();
        }

        return isValid;
    }

    // ========== Private Helper Methods ==========
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf("@");
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return "";
        }
        return email.substring(atIndex + 1);
    }

}
