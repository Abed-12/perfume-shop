package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.dto.AdminDTO;
import com.abed.perfumeshop.admin.dto.AdminUpdateRequest;
import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.admin.helper.AdminHelper;
import com.abed.perfumeshop.admin.service.AdminProfileService;
import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private final AdminRepo adminRepo;
    private final AdminHelper adminHelper;
    private final NotificationSenderFacade notificationSenderFacade;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    public Response<AdminDTO> getMyProfile() {
        Admin admin = adminHelper.getCurrentLoggedInUser();
        AdminDTO adminDTO = AdminDTO.builder()
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .build();

        return Response.<AdminDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("user.retrieved")
                .data(adminDTO)
                .build();
    }

    @Override
    public Response<?> updateMyProfile(AdminUpdateRequest adminUpdateRequest) {
        Admin admin = adminHelper.getCurrentLoggedInUser();

        if (adminRepo.existsByEmailAndIdNot(adminUpdateRequest.getEmail(), admin.getId())) {
            throw new AlreadyExistsException("user.email.already.exists");
        }

        admin.setFirstName(adminUpdateRequest.getFirstName());
        admin.setLastName(adminUpdateRequest.getLastName());
        admin.setEmail(adminUpdateRequest.getEmail());

        adminRepo.save(admin);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("user.profile.updated")
                .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
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
                .templateName("password-change")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO, null);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("auth.password.changed.success")
                .build();
    }

}
