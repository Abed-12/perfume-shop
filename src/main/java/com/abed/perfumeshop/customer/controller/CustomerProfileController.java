package com.abed.perfumeshop.customer.controller;

import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import com.abed.perfumeshop.customer.dto.CustomerUpdateRequest;
import com.abed.perfumeshop.customer.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping
    public ResponseEntity<Response<CustomerDTO>> getMyProfile(){
        return ResponseEntity.ok(customerProfileService.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<Response<?>> updateMyProfile(@RequestBody @Valid CustomerUpdateRequest customerUpdateRequest){
        return ResponseEntity.ok(customerProfileService.updateMyProfile(customerUpdateRequest));
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<?>> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest){
        return ResponseEntity.ok(customerProfileService.updatePassword(updatePasswordRequest));
    }

}
