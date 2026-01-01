package com.abed.perfumeshop.order.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuestOrderResponseDTO {

    private String trackingToken;

}
