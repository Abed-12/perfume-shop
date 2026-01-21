package com.abed.perfumeshop.common.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgotPasswordRequest {

    @Email(message = "{auth.email.invalid}")
    @NotBlank(message = "{auth.email.required}")
    private String email;

}
