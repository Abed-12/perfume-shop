package com.abed.perfumeshop.customer.service.impl;

import com.abed.perfumeshop.common.dto.UpdatePasswordRequest;
import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.exception.AlreadyExistsException;
import com.abed.perfumeshop.common.exception.BadRequestException;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import com.abed.perfumeshop.customer.dto.CustomerDTO;
import com.abed.perfumeshop.customer.dto.CustomerUpdateRequest;
import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.customer.helper.CustomerHelper;
import com.abed.perfumeshop.customer.service.CustomerProfileService;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
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
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerRepo customerRepo;
    private final CustomerHelper customerHelper;
    private final NotificationSenderFacade notificationSenderFacade;
    private final EnumLocalizationService enumLocalizationService;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    public CustomerDTO getMyProfile() {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        return CustomerDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .alternativePhoneNumber(customer.getAlternativePhoneNumber())
                .governorate(enumLocalizationService.getLocalizedName(customer.getGovernorate()))
                .address(customer.getAddress())
                .build();
    }

    @Override
    public void updateMyProfile(CustomerUpdateRequest customerUpdateRequest) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        if (customerRepo.existsByEmailAndIdNot(customerUpdateRequest.getEmail(), customer.getId())) {
            throw new AlreadyExistsException("user.email.already.exists");
        }

        customer.setFirstName(customerUpdateRequest.getFirstName());
        customer.setLastName(customerUpdateRequest.getLastName());
        customer.setEmail(customerUpdateRequest.getEmail());
        customer.setPhoneNumber(customerUpdateRequest.getPhoneNumber());

        customer.setAlternativePhoneNumber(customerUpdateRequest.getAlternativePhoneNumber());

        customer.setGovernorate(customerUpdateRequest.getGovernorate());
        customer.setAddress(customerUpdateRequest.getAddress());

        customerRepo.save(customer);
    }

    @Override
    public void updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        Customer customer = customerHelper.getCurrentLoggedInUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        // Validate the old password
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())){
            throw new BadRequestException("auth.password.old.invalid");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));

        customerRepo.save(customer);

        // send password change confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", customer.getFirstName() + " " + customer.getLastName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(messageSource.getMessage("notification.password.changed.subject", null, LocaleContextHolder.getLocale()))
                .templateName(LocaleContextHolder.getLocale().getLanguage() + "/password-change")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .build();

        notificationSenderFacade.send(notificationDTO);
    }

}
