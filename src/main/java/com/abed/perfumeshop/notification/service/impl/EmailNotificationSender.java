package com.abed.perfumeshop.notification.service.impl;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.notification.dto.NotificationDTO;
import com.abed.perfumeshop.notification.entity.Notification;
import com.abed.perfumeshop.notification.repo.NotificationRepo;
import com.abed.perfumeshop.notification.service.NotificationSender;
import com.abed.perfumeshop.order.entity.Order;
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
    public void send(NotificationDTO notificationDTO, Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());

            // Use template if provided
            if (notificationDTO.getTemplateName() != null){
                Context context = new Context();
                context.setVariables(notificationDTO.getTemplateVariables());
                String htmlContent = templateEngine.process(notificationDTO.getTemplateName(), context);
                helper.setText(htmlContent , true);
            } else {
                // If no template send text body directly
                helper.setText(notificationDTO.getBody(), true);
            }

            mailSender.send(mimeMessage);

            // Save to our database table
            Notification notificationToSave = Notification.builder()
                    .subject(notificationDTO.getSubject())
                    .recipient(notificationDTO.getRecipient())
                    .body(notificationDTO.getBody())
                    .type(NotificationType.EMAIL)
                    .order(order)
                    .build();

            notificationRepo.save(notificationToSave);

        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

}
