package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.order.dto.*;

public interface CustomerOrderService {

    OrderResponseDTO createOrder(CreateCustomerOrderRequest createCustomerOrderRequest);

    PageResponse<OrderSummaryDTO> getOrders(int page, int size, OrderStatus status);

    CustomerOrderDetailDTO getCustomerOrderByOrderNumber(String orderNumber);

    void cancelOrder(String orderNumber, CancelCustomerOrderRequest cancelCustomerOrderRequest);

}
