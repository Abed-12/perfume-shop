package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.customer.dto.CustomerDTO;

public interface AdminCustomerService {

    PageResponse<CustomerDTO> getCustomers(int page, int size, String email);

}
