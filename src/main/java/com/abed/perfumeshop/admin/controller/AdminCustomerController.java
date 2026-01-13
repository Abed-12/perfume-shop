package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminCustomerService;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<CustomerDTO>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String email
    ){
        PageResponse<CustomerDTO> pageResponse = adminCustomerService.getCustomers(page, size, email);

        return ResponseEntity.ok(
                Response.<PageResponse<CustomerDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("customers.retrieved")
                        .data(pageResponse)
                        .build()
        );
    }

}
