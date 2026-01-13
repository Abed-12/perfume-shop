package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import com.abed.perfumeshop.customer.dto.CustomerUpdateRequest;

public interface CustomerProfileService {

    CustomerDTO getMyProfile();

    void updateMyProfile(CustomerUpdateRequest customerUpdateRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
