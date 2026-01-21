package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.request.UpdatePasswordRequest;
import com.abed.perfumeshop.customer.dto.response.CustomerDTO;
import com.abed.perfumeshop.customer.dto.request.CustomerUpdateRequest;

public interface CustomerProfileService {

    CustomerDTO getMyProfile();

    void updateMyProfile(CustomerUpdateRequest customerUpdateRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
