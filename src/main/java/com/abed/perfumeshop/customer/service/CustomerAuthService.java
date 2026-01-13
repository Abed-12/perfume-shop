package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
import com.abed.perfumeshop.customer.dto.CustomerRegisterRequest;

public interface CustomerAuthService {

    void register(CustomerRegisterRequest customerRegisterRequest);

    LoginResponse login(LoginRequest loginRequest);

    void forgotPassword(String email);

    void resetPassword(PasswordResetRequest passwordResetRequest);

}
