package com.abed.perfumeshop.customer.controller;

import com.abed.perfumeshop.common.dto.ForgotPasswordRequest;
import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.CustomerRegisterRequest;
import com.abed.perfumeshop.customer.service.CustomerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> register(@RequestBody @Valid CustomerRegisterRequest customerRegisterRequest){
        return ResponseEntity.ok(customerAuthService.register(customerRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(customerAuthService.login(loginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<?>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest){
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(customerAuthService.forgotPassword(forgotPasswordRequest.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest){
        return ResponseEntity.ok(customerAuthService.resetPassword(passwordResetRequest));
    }

}
