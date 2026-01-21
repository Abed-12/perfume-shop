package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminAuthService;
import com.abed.perfumeshop.common.dto.request.ForgotPasswordRequest;
import com.abed.perfumeshop.common.dto.request.LoginRequest;
import com.abed.perfumeshop.common.dto.response.LoginResponse;
import com.abed.perfumeshop.common.dto.request.PasswordResetRequest;
import com.abed.perfumeshop.common.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = adminAuthService.login(loginRequest);

        return ResponseEntity.ok(
                Response.<LoginResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("auth.login.success")
                        .data(loginResponse)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        adminAuthService.forgotPassword(forgotPasswordRequest.getEmail());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(
                        Response.<Void>builder()
                                .statusCode(HttpStatus.ACCEPTED.value())
                                .message("notification.password.reset.sent")
                                .build()
                );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<Void>> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        adminAuthService.resetPassword(passwordResetRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("notification.password.update")
                        .build()
        );
    }

}
