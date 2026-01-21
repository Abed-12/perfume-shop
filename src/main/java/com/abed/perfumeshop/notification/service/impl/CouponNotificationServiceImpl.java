package com.abed.perfumeshop.notification.service.impl;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.projection.EmailRecipientProjection;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.customer.repo.CustomerRepo;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.service.CouponNotificationService ;
import com.abed.perfumeshop.notification.service.NotificationSenderFacade;
import com.abed.perfumeshop.order.repo.GuestOrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponNotificationServiceImpl implements CouponNotificationService  {

    private final CustomerRepo customerRepo;
    private final GuestOrderRepo guestOrderRepo;
    private final NotificationSenderFacade notificationSenderFacade;
    private final MessageSource messageSource;

    @Override
    @Async
    public void sendCouponToAllUsers(Coupon coupon, Locale locale) {
        List<EmailRecipientProjection> customers = customerRepo.findAllEmailProjections();
        List<EmailRecipientProjection> guests = guestOrderRepo.findAllDistinctEmailProjections();

        Map<String, EmailRecipientProjection> recipientsMap = new HashMap<>();
        guests.forEach(guest -> recipientsMap.put(guest.getEmail(), guest));
        customers.forEach(customer -> recipientsMap.put(customer.getEmail(), customer));

        List<EmailRecipientProjection> allRecipients = new ArrayList<>(recipientsMap.values());

        log.info("Sending to {} recipients", allRecipients.size());

        int success = 0, failed = 0;
        for (EmailRecipientProjection recipient : allRecipients) {
            try {
                sendCouponEmail(recipient, coupon, locale);
                success++;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Email sending interrupted", e);
                break;
            } catch (Exception e) {
                failed++;
                log.error("Failed to send to: {}", recipient.getEmail(), e);
            }
        }

        log.info("Email campaign done: {} success, {} failed", success, failed);
    }

    private void sendCouponEmail(EmailRecipientProjection recipient, Coupon coupon, Locale locale) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("customerName", recipient.getName());
        templateVariables.put("couponCode", coupon.getCode());
        templateVariables.put("discountValue", coupon.getDiscountValue());
        templateVariables.put("discountType", coupon.getDiscountType().toString());
        templateVariables.put("expiryDate", coupon.getExpiryDate());
        templateVariables.put("maxUsage", coupon.getMaxUsage());
        templateVariables.put("needsLogin", !recipient.getIsCustomer());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(recipient.getEmail())
                .subject(messageSource.getMessage("notification.coupon.subject", null, locale))
                .templateName(locale.getLanguage() + "/coupon-distribution")
                .templateVariables(templateVariables)
                .type(NotificationType.EMAIL)
                .coupon(coupon)
                .build();

        notificationSenderFacade.send(notificationDTO);
    }

}
