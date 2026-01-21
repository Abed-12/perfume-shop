package com.abed.perfumeshop.notification.service;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.notification.dto.response.NotificationDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationSenderFacade {

    private final Map<NotificationType, NotificationSender> senders;

    public NotificationSenderFacade(List<NotificationSender> senderList) {
        senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::getType, s -> s));
    }

    public void send(NotificationDTO notificationDTO) {
        NotificationSender sender = senders.get(notificationDTO.getType());

        if (sender != null) {
            sender.send(notificationDTO);
        } else {
            throw new NotFoundException("notification.sender.facade.not.found", new Object[]{notificationDTO.getType()});
        }
    }

}
