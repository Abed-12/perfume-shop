package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;

public interface AdminAuthService {

    LoginResponse login(LoginRequest loginRequest);

    void forgotPassword(String email);

    void resetPassword(PasswordResetRequest passwordResetRequest);

}
