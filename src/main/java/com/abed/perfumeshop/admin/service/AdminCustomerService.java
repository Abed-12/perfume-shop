package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.customer.dto.response.CustomerDTO;

public interface AdminCustomerService {

    PageResponse<CustomerDTO> getCustomers(int page, int size, String email);

}
