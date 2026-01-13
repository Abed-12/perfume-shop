package com.abed.perfumeshop.order.service;

import com.abed.perfumeshop.order.dto.CancelGuestOrderRequest;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.OrderResponseDTO;

public interface GuestOrderService {

    OrderResponseDTO createGuestOrder(CreateGuestOrderRequest createGuestOrderRequest);

    GuestOrderDetailDTO getGuestOrderByEmailAndOrderNumber(String email, String orderNumber);

    void cancelOrder(String orderNumber, CancelGuestOrderRequest cancelGuestOrderRequest);

}
