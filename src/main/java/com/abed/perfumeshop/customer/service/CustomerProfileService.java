package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import com.abed.perfumeshop.customer.dto.CustomerUpdateRequest;

public interface CustomerProfileService {

    Response<CustomerDTO> getMyProfile();

    Response<?> updateMyProfile(CustomerUpdateRequest customerUpdateRequest);

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

}
