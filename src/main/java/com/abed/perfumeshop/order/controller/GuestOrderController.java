package com.abed.perfumeshop.order.controller;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.order.dto.CancelGuestOrderRequest;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.GuestOrderDetailDTO;
import com.abed.perfumeshop.order.dto.OrderResponseDTO;
import com.abed.perfumeshop.order.service.GuestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/guest-orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    public ResponseEntity<Response<OrderResponseDTO>> createGuestOrder(@RequestBody @Valid CreateGuestOrderRequest createGuestOrderRequest) {
        OrderResponseDTO orderResponseDTO = guestOrderService.createGuestOrder(createGuestOrderRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.<OrderResponseDTO>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("order.created.successfully")
                        .data(orderResponseDTO)
                        .build()
                );
    }

    @GetMapping("/track")
    public ResponseEntity<Response<GuestOrderDetailDTO>> getGuestOrderByEmailAndOrderNumber(
            @RequestParam String email,
            @RequestParam String orderNumber
    ){
        GuestOrderDetailDTO guestOrderDetailDTO = guestOrderService.getGuestOrderByEmailAndOrderNumber(email, orderNumber);

        return ResponseEntity.ok(
                Response.<GuestOrderDetailDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.retrieved.successfully")
                        .data(guestOrderDetailDTO)
                        .build()
        );
    }

    @PatchMapping("/{orderNumber}/cancel")
    public ResponseEntity<Response<Void>> cancelOrder(
            @PathVariable String orderNumber,
            @RequestBody @Valid CancelGuestOrderRequest cancelGuestOrderRequest
    ) {
        guestOrderService.cancelOrder(orderNumber, cancelGuestOrderRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("order.cancelled.successfully")
                        .build()
        );
    }

}
