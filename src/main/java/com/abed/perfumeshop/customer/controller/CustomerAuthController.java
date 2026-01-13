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
    public ResponseEntity<Response<Void>> register(@RequestBody @Valid CustomerRegisterRequest customerRegisterRequest) {
        customerAuthService.register(customerRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Response.<Void>builder()
                                .statusCode(HttpStatus.CREATED.value())
                                .message("auth.registration.success")
                                .build()
                );
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest){
        LoginResponse loginResponse = customerAuthService.login(loginRequest);

        return ResponseEntity.ok(
                Response.<LoginResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("auth.login.success")
                        .data(loginResponse)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest){
        customerAuthService.forgotPassword(forgotPasswordRequest.getEmail());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(
                        Response.<Void>builder()
                                .statusCode(HttpStatus.ACCEPTED.value())
                                .message("notification.password.reset.sent")
                                .build()
                );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<Void>> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest){
        customerAuthService.resetPassword(passwordResetRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("notification.password.update")
                        .build()
        );
    }

}
