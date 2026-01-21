package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.order.dto.response.AdminOrderSummaryDTO;
import com.abed.perfumeshop.order.dto.response.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.request.UpdateOrderStatusRequest;

public interface AdminOrderService {

    PageResponse<AdminOrderSummaryDTO> getCustomerOrders(int page, int size, OrderStatus status);

    PageResponse<AdminOrderSummaryDTO> getGuestOrders(int page, int size, OrderStatus status);

    CustomerOrderDetailDTO getCustomerOrderByOrderNumber(String orderNumber);

    GuestOrderDetailDTO getGuestOrderByEmailAndOrderNumber(String email, String orderNumber);

    void updateOrderStatus(String orderNumber, UpdateOrderStatusRequest updateOrderStatusRequest);

}
