package com.abed.perfumeshop.config.security.service;

import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.abed.perfumeshop.config.security.SecurityConstants.*;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final AdminRepo adminRepo;
    private final CustomerRepo customerRepo;

    public boolean existsByEmailAndRole(String email, String role) {
        return switch (role) {
            case ROLE_ADMIN -> adminRepo.findByEmail(email).isPresent();

            case ROLE_CUSTOMER -> customerRepo.findByEmail(email).isPresent();

            default -> false;
        };
    }

}
