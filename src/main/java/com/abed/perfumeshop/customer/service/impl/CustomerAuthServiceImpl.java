package com.abed.perfumeshop.customer.service.impl;

import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.common.dto.request.LoginRequest;
import com.abed.perfumeshop.common.dto.response.LoginResponse;
import com.abed.perfumeshop.common.dto.request.PasswordResetRequest;
import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.customer.dto.request.CustomerRegisterRequest;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.customer.service.CustomerAuthService;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.entity.GuestOrder;
import com.abed.perfumeshop.order.repo.GuestOrderRepo;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final CustomerRepo customerRepo;
    private final AdminRepo adminRepo;
    private final GuestOrderRepo guestOrderRepo;
    private final NotificationSenderFacade notificationSenderFacade;
    private final PasswordResetCodeRepo passwordResetCodeRepo;
    private final CodeGenerator codeGenerator;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    @Transactional
    public void register(CustomerRegisterRequest customerRegisterRequest) {
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

        // Auto-claim unclaimed guest orders with same email
        List<GuestOrder> unclaimedOrders = guestOrderRepo.findByEmailAndClaimedByCustomerIsNull(email);

        if (!unclaimedOrders.isEmpty()){
            unclaimedOrders.forEach(guestOrder -> {
                guestOrder.setClaimedByCustomer(customer);
                guestOrder.setClaimedAt(LocalDateTime.now());
            });
            guestOrderRepo.saveAll(unclaimedOrders);
        }

        // Send welcome email of the user
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customer.getFirstName() + " " + customer.getLastName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.welcome.subject", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/welcome")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("exception.invalid.credentials"));

        if(!passwordEncoder.matches(password, customer.getPassword())){
            throw new BadCredentialsException("exception.invalid.credentials");
        }

        String token = jwtService.generateToken(customer.getEmail(), UserType.CUSTOMER.name());

         return LoginResponse.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
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
        templateVariables.put("name", customer.getFirstName() + " " + customer.getLastName());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(customer.getEmail())
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
        Customer customer = customerRepo.findById(resetCode.getUserId())
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepo.save(customer);

        // Delete the code immediately after successful use
        passwordResetCodeRepo.delete(resetCode);

        // Send confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customer.getFirstName() + " " + customer.getLastName());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.password.update", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/password-update-confirmation")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(confirmationEmail);
    }

}
