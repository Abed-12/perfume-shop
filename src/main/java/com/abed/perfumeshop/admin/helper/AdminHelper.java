package com.abed.perfumeshop.admin.helper;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.service.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminHelper {

    private final AdminRepo adminRepo;
    private final AuthenticationHelper authenticationHelper;

    public Admin getCurrentLoggedInUser() {
        String email = authenticationHelper.getCurrentUserEmail();

        return adminRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user.not.found"));
    }

}
