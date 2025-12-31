package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminAuthService;
import com.abed.perfumeshop.common.dto.ForgotPasswordRequest;
import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
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
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(adminAuthService.login(loginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<?>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest){
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(adminAuthService.forgotPassword(forgotPasswordRequest.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest){
        return ResponseEntity.ok(adminAuthService.resetPassword(passwordResetRequest));
    }

}
