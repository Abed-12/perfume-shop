package com.abed.perfumeshop.order.dto;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.common.validation.TrustedEmailDomain;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateGuestOrderRequest {

    @NotBlank(message = "{auth.username.required}")
    private String username;

    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    @TrustedEmailDomain(message = "{auth.email.domain.not.trusted}")
    private String email;

    @NotBlank(message = "{auth.phone.required}")
    private String phoneNumber;

    private String alternativePhoneNumber;

    @NotNull(message = "{auth.governorate.required}")
    private Governorate governorate;

    @NotBlank(message = "{auth.address.required}")
    private String address;

    @Size(max = 1000, message = "{order.notes.max.size}")
    private String notes;

    @Valid
    @NotEmpty(message = "{order.items.required}")
    private List<OrderItemRequest> items;

}
