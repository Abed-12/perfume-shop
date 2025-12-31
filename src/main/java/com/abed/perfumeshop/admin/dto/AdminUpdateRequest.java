package com.abed.perfumeshop.admin.dto;

import com.abed.perfumeshop.common.validation.TrustedEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateRequest {

    @NotBlank(message = "{auth.firstname.required}")
    private String firstName;

    @NotBlank(message = "{auth.lastname.required}")
    private String lastName;

    @Email(message = "{auth.email.invalid}")
    @NotBlank(message = "{auth.email.required}")
    @TrustedEmailDomain(message = "{auth.email.domain.not.trusted}")
    private String email;

}
