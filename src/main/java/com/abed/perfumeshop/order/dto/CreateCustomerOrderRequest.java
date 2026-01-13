package com.abed.perfumeshop.order.dto;

import com.abed.perfumeshop.common.enums.Governorate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateCustomerOrderRequest {

    @NotNull(message = "{auth.governorate.required}")
    private Governorate governorate;

    @NotBlank(message = "{auth.address.required}")
    private String address;

    @NotBlank(message = "{auth.phone.required}")
    private String phoneNumber;

    private String alternativePhoneNumber;

    @Size(max = 1000, message = "{order.notes.max.size}")
    private String notes;

    private String couponCode;

    @Valid
    @NotEmpty(message = "{order.items.required}")
    private List<OrderItemRequest> items;

}
