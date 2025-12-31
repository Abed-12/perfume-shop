package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.service.AdminCustomerService;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final AdminHelper adminHelper;
    private final CustomerRepo customerRepo;
    private final EnumLocalizationService enumLocalizationService;

    @Override
    public Response<PageResponse<CustomerDTO>> getCustomers(int page, int size, String email) {
        adminHelper.getCurrentLoggedInUser();

        Page<Customer> customers = customerRepo.findAllByFilter(email, PageRequest.of(page, size));
        Page<CustomerDTO> customerDTOSPage = customers.map(this::mapToDTO);

        PageResponse<CustomerDTO> pageResponse = PageResponse.<CustomerDTO>builder()
                .content(customerDTOSPage.getContent())
                .page(PageResponse.PageInfo.builder()
                        .size(customerDTOSPage.getSize())
                        .number(customerDTOSPage.getNumber())
                        .totalElements(customerDTOSPage.getTotalElements())
                        .totalPages(customerDTOSPage.getTotalPages())
                        .build())
                .build();

        return Response.<PageResponse<CustomerDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("customers.retrieved")
                .data(pageResponse)
                .build();
    }

    // ========== Private Helper Methods ==========
    private CustomerDTO mapToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .alternativePhoneNumber(customer.getAlternativePhoneNumber())
                .governorate(
                        enumLocalizationService.getLocalizedName(customer.getGovernorate())
                )
                .address(customer.getAddress())
                .build();
    }

}
