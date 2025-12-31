package com.abed.perfumeshop.customer.helper;

import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.AuthenticationHelper;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerHelper {

    private final CustomerRepo customerRepo;
    private final AuthenticationHelper authenticationHelper;

    public Customer getCurrentLoggedInUser() {
        String email = authenticationHelper.getCurrentUserEmail();

        return customerRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user.not.found"));
    }

}
