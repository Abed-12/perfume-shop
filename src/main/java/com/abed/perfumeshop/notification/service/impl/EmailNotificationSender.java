package com.abed.perfumeshop.notification.service.impl;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.response.EmailNotificationDTO;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import com.abed.perfumeshop.notification.entity.Notification;
import com.abed.perfumeshop.notification.repo.NotificationRepo;
import com.abed.perfumeshop.notification.service.NotificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final NotificationRepo notificationRepo;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void send(NotificationDTO notificationDTO) {
        // Validate DTO type
        if (!(notificationDTO instanceof EmailNotificationDTO emailNotificationDTO)) {
            log.error("Invalid DTO type for EmailNotificationSender. Expected EmailNotificationDTO but got {}",
                    notificationDTO.getClass().getSimpleName());
            return;
        }

        try {
            // Send email to recipient
            sendEmail(emailNotificationDTO);

            // Save notification to database
            saveNotification(emailNotificationDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    // ========== Private Helper Methods ==========
    private void sendEmail(EmailNotificationDTO emailNotificationDTO) throws MessagingException {
        // Create and configure email message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(emailNotificationDTO.getRecipient());
        helper.setSubject(emailNotificationDTO.getSubject());

        // Use template if provided
        if (emailNotificationDTO.getTemplateName() != null){
            Context context = new Context();
            context.setVariables(emailNotificationDTO.getTemplateVariables());
            String htmlContent = templateEngine.process(emailNotificationDTO.getTemplateName(), context);
            helper.setText(htmlContent , true);
        } else {
            // If no template send text body directly
            helper.setText(emailNotificationDTO.getBody(), true);
        }

        // Send email
        mailSender.send(mimeMessage);
    }

    private void saveNotification(EmailNotificationDTO emailNotificationDTO) {
        try {
            Notification notificationToSave = Notification.builder()
                    .subject(emailNotificationDTO.getSubject())
                    .recipient(emailNotificationDTO.getRecipient())
                    .body(emailNotificationDTO.getBody())
                    .type(NotificationType.EMAIL)
                    .order(emailNotificationDTO.getOrder())
                    .coupon(emailNotificationDTO.getCoupon())
                    .build();

            notificationRepo.save(notificationToSave);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
