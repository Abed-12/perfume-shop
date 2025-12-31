package com.abed.perfumeshop.customer.service.impl;

import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.common.dto.LoginRequest;
import com.abed.perfumeshop.common.dto.LoginResponse;
import com.abed.perfumeshop.common.dto.PasswordResetRequest;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.dto.CustomerRegisterRequest;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.customer.service.CustomerAuthService;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final CustomerRepo customerRepo;
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
    public Response<?> register(CustomerRegisterRequest customerRegisterRequest) {
        String email = customerRegisterRequest.getEmail();
        if (customerRepo.existsByEmail(email) || adminRepo.existsByEmail(email)) {
            throw new AlreadyExistsException("user.email.already.exists");
        }

        Customer customer = Customer.builder()
                .firstName(customerRegisterRequest.getFirstName())
                .lastName(customerRegisterRequest.getLastName())
                .email(email)
                .phoneNumber(customerRegisterRequest.getPhoneNumber())
                .alternativePhoneNumber(customerRegisterRequest.getAlternativePhoneNumber())
                .governorate(customerRegisterRequest.getGovernorate())
                .address(customerRegisterRequest.getAddress())
                .password(passwordEncoder.encode(customerRegisterRequest.getPassword()))
                .build();

        customerRepo.save(customer);

        // Send welcome email of the user
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", customer.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.welcome.subject", null, LocaleContextHolder.getLocale()))
                .templateName("welcome")
                .templateVariables(vars)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO, null);

        return Response.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("auth.registration.success")
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("exception.invalid.credentials"));

        if(!passwordEncoder.matches(password, customer.getPassword())){
            throw new BadCredentialsException("exception.invalid.credentials");
        }

        String token = jwtService.generateToken(customer.getEmail(), UserType.CUSTOMER.name());

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("auth.login.success")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgotPassword(String email) {
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user.not.found"));
        passwordResetCodeRepo.deleteByUserIdAndUserType(customer.getId(), UserType.CUSTOMER);

        String code = codeGenerator.generateUniqueCode(c -> passwordResetCodeRepo.findByCode(c).isPresent());

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .userType(UserType.CUSTOMER)
                .userId(customer.getId())
                .build();

        passwordResetCodeRepo.save(resetCode);

        // Send email reset link out
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customer.getFirstName());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.password.reset.subject", null, LocaleContextHolder.getLocale()))
                .templateName("password-reset")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO, null);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("notification.password.reset.sent")
                .build();
    }

    @Override
    @Transactional
    public Response<?> resetPassword(PasswordResetRequest passwordResetRequest) {
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
        Customer customer = customerRepo.findById(resetCode.getUserId())
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepo.save(customer);

        // Delete the code immediately after successful use
        passwordResetCodeRepo.delete(resetCode);

        // Send confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customer.getFirstName());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.password.update", null, LocaleContextHolder.getLocale()))
                .templateName("password-update-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(confirmationEmail, null);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("notification.password.update")
                .build();
    }

}
