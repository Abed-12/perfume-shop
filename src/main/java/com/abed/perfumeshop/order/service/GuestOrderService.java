package com.abed.perfumeshop.order.service;

import com.abed.perfumeshop.order.dto.request.CancelGuestOrderRequest;
import com.abed.perfumeshop.order.dto.request.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.response.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.OrderResponseDTO;

public interface GuestOrderService {

    OrderResponseDTO createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest);

    GuestOrderDetailDTO getGuestOrderByEmailAndOrderNumber(String email, String orderNumber);

    void cancelOrder(String orderNumber, CancelGuestOrderRequest cancelGuestOrderRequest);

}
