package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.admin.service.AdminAuthService;
import com.abed.perfumeshop.common.dto.request.LoginRequest;
import com.abed.perfumeshop.common.dto.response.LoginResponse;
import com.abed.perfumeshop.common.dto.request.PasswordResetRequest;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.passwordResetCode.entity.PasswordResetCode;
import com.abed.perfumeshop.passwordResetCode.repo.PasswordResetCodeRepo;
import com.abed.perfumeshop.passwordResetCode.service.CodeGenerator;
import com.abed.perfumeshop.config.security.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminRepo adminRepo;
    private final NotificationSenderFacade notificationSenderFacade;
    private final PasswordResetCodeRepo passwordResetCodeRepo;
    private final CodeGenerator codeGenerator;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("exception.invalid.credentials"));

        if(!passwordEncoder.matches(password, admin.getPassword())){
            throw new BadCredentialsException("exception.invalid.credentials");
        }

        String token = jwtService.generateToken(admin.getEmail(), UserType.ADMIN.name());

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user.not.found"));
        passwordResetCodeRepo.deleteByUserIdAndUserType(admin.getId(), UserType.ADMIN);

        String code = codeGenerator.generateUniqueCode(c -> passwordResetCodeRepo.findByCode(c).isPresent());

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .userType(UserType.ADMIN)
                .userId(admin.getId())
                .build();

        passwordResetCodeRepo.save(resetCode);

        // Send email reset link out
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", admin.getFirstName());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(admin.getEmail())
                .subject(messageSource.getMessage("notification.password.reset.subject", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/password-reset")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        String code = passwordResetRequest.getCode();
        String newPassword = passwordResetRequest.getNewPassword();

        // Find and validate code
        PasswordResetCode resetCode = passwordResetCodeRepo.findByCode(code)
                .orElseThrow(() -> new BadRequestException("password.reset.code.invalid"));

        // Check expiration first
        if (resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepo.delete(resetCode);
            throw new BadRequestException("password.reset.code.expired");
        }

        // Update the password
        Admin admin = adminRepo.findById(resetCode.getUserId())
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepo.save(admin);

        // Delete the code immediately after successful use
        passwordResetCodeRepo.delete(resetCode);

        // Send confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", admin.getFirstName());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
                .recipient(admin.getEmail())
                .subject(messageSource.getMessage("notification.password.update", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/password-update-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(confirmationEmail);
    }

}
