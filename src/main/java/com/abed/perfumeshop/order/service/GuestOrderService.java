package com.abed.perfumeshop.order.service;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.GuestOrderResponseDTO;

public interface GuestOrderService {

    Response<GuestOrderResponseDTO> createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest);

}
