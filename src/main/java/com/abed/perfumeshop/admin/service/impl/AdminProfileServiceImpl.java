package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.dto.response.AdminDTO;
import com.abed.perfumeshop.admin.dto.request.AdminUpdateRequest;
import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminProfileService;
import com.abed.perfumeshop.common.dto.request.UpdatePasswordRequest;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private final AdminRepo adminRepo;
    private final AdminHelper adminHelper;
    private final CustomerRepo customerRepo;
    private final NotificationSenderFacade notificationSenderFacade;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    public AdminDTO getMyProfile() {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        return AdminDTO.builder()
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .build();
    }

    @Override
    public void updateMyProfile(AdminUpdateRequest adminUpdateRequest) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        if (adminRepo.existsByEmailAndIdNot(adminUpdateRequest.getEmail(), admin.getId())
                || customerRepo.existsByEmail(adminUpdateRequest.getEmail())) {
            throw new AlreadyExistsException("user.email.already.exists");
        }

        admin.setFirstName(adminUpdateRequest.getFirstName());
        admin.setLastName(adminUpdateRequest.getLastName());
        admin.setEmail(adminUpdateRequest.getEmail());

        adminRepo.save(admin);
    }

    @Override
    public void updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        // Validate the old password
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())){
            throw new BadRequestException("auth.password.old.invalid");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));

        adminRepo.save(admin);

        // send password change confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", admin.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(admin.getEmail())
                .subject(messageSource.getMessage("notification.password.changed.subject", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/password-change")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO);
    }

}
