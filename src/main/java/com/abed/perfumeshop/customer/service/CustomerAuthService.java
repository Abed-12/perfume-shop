package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.request.LoginRequest;
import com.abed.perfumeshop.common.dto.response.LoginResponse;
import com.abed.perfumeshop.common.dto.request.PasswordResetRequest;
import com.abed.perfumeshop.customer.dto.request.CustomerRegisterRequest;

public interface CustomerAuthService {

    void register(CustomerRegisterRequest customerRegisterRequest);

    LoginResponse login(LoginRequest loginRequest);

    void forgotPassword(String email);

    void resetPassword(PasswordResetRequest passwordResetRequest);

}
