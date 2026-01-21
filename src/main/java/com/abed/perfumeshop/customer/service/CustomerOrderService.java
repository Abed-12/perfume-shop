package com.abed.perfumeshop.customer.service;

import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.order.dto.request.CancelCustomerOrderRequest;
import com.abed.perfumeshop.order.dto.request.CreateCustomerOrderRequest;
import com.abed.perfumeshop.order.dto.response.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.response.OrderResponseDTO;
import com.abed.perfumeshop.order.dto.response.OrderSummaryDTO;

public interface CustomerOrderService {

    OrderResponseDTO createOrder(CreateCustomerOrderRequest createCustomerOrderRequest);

    PageResponse<OrderSummaryDTO> getOrders(int page, int size, OrderStatus status);

    CustomerOrderDetailDTO getCustomerOrderByOrderNumber(String orderNumber);

    void cancelOrder(String orderNumber, CancelCustomerOrderRequest cancelCustomerOrderRequest);

}
