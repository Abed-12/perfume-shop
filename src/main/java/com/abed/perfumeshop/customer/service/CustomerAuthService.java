package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
import com.abed.perfumeshop.customer.dto.CustomerRegisterRequest;
import com.abed.perfumeshop.common.res.Response;

public interface CustomerAuthService {

    Response<?> register(CustomerRegisterRequest customerRegisterRequest);

    Response<LoginResponse> login(LoginRequest loginRequest);

    Response<?> forgotPassword(String email);

    Response<?> resetPassword(PasswordResetRequest passwordResetRequest);

}
