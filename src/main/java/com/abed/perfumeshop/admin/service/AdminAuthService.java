package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
import com.abed.perfumeshop.common.res.Response;

public interface AdminAuthService {

    Response<LoginResponse> login(LoginRequest loginRequest);

    Response<?> forgotPassword(String email);

    Response<?> resetPassword(PasswordResetRequest passwordResetRequest);

}
