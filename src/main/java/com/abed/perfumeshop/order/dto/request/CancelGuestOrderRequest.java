package com.abed.perfumeshop.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelGuestOrderRequest {

    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    private String email;

    @Size(max = 500, message = "order.cancellation.reason.too.long")
    private String cancellationReason;

}
