package com.abed.perfumeshop.customer.controller;

import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.customer.service.CustomerOrderService;
import com.abed.perfumeshop.order.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @PostMapping
    public ResponseEntity<Response<OrderResponseDTO>> createOrder(@RequestBody @Valid CreateCustomerOrderRequest createCustomerOrderRequest){
        OrderResponseDTO orderResponseDTO = customerOrderService.createOrder(createCustomerOrderRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Response.<OrderResponseDTO>builder()
                                .statusCode(HttpStatus.CREATED.value())
                                .message("order.created.successfully")
                                .data(orderResponseDTO)
                                .build()
                );
    }

    @GetMapping
    public ResponseEntity<Response<PageResponse<OrderSummaryDTO>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        PageResponse<OrderSummaryDTO> pageResponse = customerOrderService.getOrders(page, size, status);

        return ResponseEntity.ok(
                Response.<PageResponse<OrderSummaryDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("orders.retrieved.successfully")
                        .data(pageResponse)
                        .build()
        );
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Response<CustomerOrderDetailDTO>> getCustomerOrderByOrderNumber(@PathVariable String orderNumber) {
        CustomerOrderDetailDTO customerOrderDetailDTO = customerOrderService.getCustomerOrderByOrderNumber(orderNumber);

        return ResponseEntity.ok(
                Response.<CustomerOrderDetailDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.retrieved.successfully")
                        .data(customerOrderDetailDTO)
                        .build()
        );
    }

    @PatchMapping("/{orderNumber}/cancel")
    public ResponseEntity<Response<Void>> cancelOrder(
            @PathVariable String orderNumber,
            @RequestBody(required = false) @Valid CancelCustomerOrderRequest cancelCustomerOrderRequest
    ) {
        customerOrderService.cancelOrder(orderNumber, cancelCustomerOrderRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.cancelled.successfully")
                        .build()
        );
    }

}
