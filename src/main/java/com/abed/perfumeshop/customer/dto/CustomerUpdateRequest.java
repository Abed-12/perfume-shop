package com.abed.perfumeshop.customer.dto;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.common.validation.TrustedEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerUpdateRequest {

    @NotBlank(message = "{auth.firstname.required}")
    private String firstName;

    @NotBlank(message = "{auth.lastname.required}")
    private String lastName;

    @Email(message = "{auth.email.invalid}")
    @NotBlank(message = "{auth.email.required}")
    @TrustedEmailDomain(message = "{auth.email.domain.not.trusted}")
    private String email;

    @NotBlank(message = "{auth.phone.required}")
    private String phoneNumber;

    private String alternativePhoneNumber;

    @NotNull(message = "{auth.governorate.required}")
    private Governorate governorate;

    @NotBlank(message = "{auth.address.required}")
    private String address;

}
