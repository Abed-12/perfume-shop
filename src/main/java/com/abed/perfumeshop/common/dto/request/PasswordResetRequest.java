package com.abed.perfumeshop.common.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordResetRequest {

    @NotBlank(message = "{auth.reset.code.required}")
    private String code;

    @NotBlank(message = "{auth.new.password.required}")
    @Size(min = 8, message = "{auth.new.password.min.size}")
    private String newPassword;

}
