package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.request.LoginRequest;
import com.abed.perfumeshop.common.dto.response.LoginResponse;
import com.abed.perfumeshop.common.dto.request.PasswordResetRequest;

public interface AdminAuthService {

    LoginResponse login(LoginRequest loginRequest);

    void forgotPassword(String email);

    void resetPassword(PasswordResetRequest passwordResetRequest);

}
