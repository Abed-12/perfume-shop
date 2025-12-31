package com.abed.perfumeshop.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "{auth.old.password.required}")
    private String oldPassword;

    @NotBlank(message = "{auth.new.password.required}")
    @Size(min = 8, message = "{auth.new.password.min.size}")
    private String newPassword;

}
