package com.abed.perfumeshop.customer.dto.response;

import com.abed.perfumeshop.common.enums.Governorate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDTO {

    private String firstName;
    private String lastName;
    private String email;

    private String phoneNumber;
    private String alternativePhoneNumber;

    private Governorate governorate;
    private String address;

    @JsonIgnore
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
