package com.abed.perfumeshop.customer.controller;

import com.abed.perfumeshop.common.dto.request.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.response.CustomerDTO;
import com.abed.perfumeshop.customer.dto.request.CustomerUpdateRequest;
import com.abed.perfumeshop.customer.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping
    public ResponseEntity<Response<CustomerDTO>> getMyProfile(){
        CustomerDTO customerDTO = customerProfileService.getMyProfile();

        return ResponseEntity.ok(
                Response.<CustomerDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("user.retrieved")
                        .data(customerDTO)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<Response<Void>> updateMyProfile(@RequestBody @Valid CustomerUpdateRequest customerUpdateRequest){
        customerProfileService.updateMyProfile(customerUpdateRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("user.profile.updated")
                        .build()
        );
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<Void>> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest){
        customerProfileService.updatePassword(updatePasswordRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("auth.password.changed.success")
                        .build()
        );
    }

}
