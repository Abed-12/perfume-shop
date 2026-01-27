package com.abed.perfumeshop.notification.dto.response;

import com.abed.perfumeshop.common.enums.NotificationType;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NotificationDTO {

    private String subject;

    private String body;

    private NotificationType type;

    private Order order;

    private Coupon coupon;

}
