package com.abed.perfumeshop.config.app;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "email.validation")
@Data
@Validated
public class EmailValidationConfig {

    @NotEmpty(message = "Trusted domains list cannot be empty")
    private Set<String> trustedDomains;

}
