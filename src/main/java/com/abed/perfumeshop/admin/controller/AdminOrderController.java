package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminOrderService;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.enums.OrderStatus;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.order.dto.AdminOrderSummaryDTO;
import com.abed.perfumeshop.order.dto.CustomerOrderDetailDTO;
import com.abed.perfumeshop.order.dto.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/customer")
    public ResponseEntity<Response<PageResponse<AdminOrderSummaryDTO>>> getCustomerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        PageResponse<AdminOrderSummaryDTO> pageResponse = adminOrderService.getCustomerOrders(page, size, status);

        return ResponseEntity.ok(
                Response.<PageResponse<AdminOrderSummaryDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("orders.retrieved.successfully")
                        .data(pageResponse)
                        .build()
        );
    }

    @GetMapping("/guest")
    public ResponseEntity<Response<PageResponse<AdminOrderSummaryDTO>>> getGuestOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        PageResponse<AdminOrderSummaryDTO> pageResponse = adminOrderService.getGuestOrders(page, size, status);

        return ResponseEntity.ok(
                Response.<PageResponse<AdminOrderSummaryDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("orders.retrieved.successfully")
                        .data(pageResponse)
                        .build()
        );
    }

    @GetMapping("/customer/{orderNumber}")
    public ResponseEntity<Response<CustomerOrderDetailDTO>> getCustomerOrderDetails(@PathVariable String orderNumber) {
        CustomerOrderDetailDTO customerOrderDetailDTO = adminOrderService.getCustomerOrderByOrderNumber(orderNumber);

        return ResponseEntity.ok(
                Response.<CustomerOrderDetailDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.retrieved.successfully")
                        .data(customerOrderDetailDTO)
                        .build()
        );
    }

    @GetMapping("/guest/{orderNumber}")
    public ResponseEntity<Response<GuestOrderDetailDTO>> getGuestOrderDetails(
            @PathVariable String orderNumber,
            @RequestParam String email
    ) {
        GuestOrderDetailDTO guestOrderDetailDTO = adminOrderService.getGuestOrderByEmailAndOrderNumber(email, orderNumber);

        return ResponseEntity.ok(
                Response.<GuestOrderDetailDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.retrieved.successfully")
                        .data(guestOrderDetailDTO)
                        .build()
        );
    }

    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<Response<Void>> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestBody @Valid UpdateOrderStatusRequest updateOrderStatusRequest
    ) {
        adminOrderService.updateOrderStatus(orderNumber, updateOrderStatusRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.status.updated.successfully")
                        .build()
        );
    }

}
