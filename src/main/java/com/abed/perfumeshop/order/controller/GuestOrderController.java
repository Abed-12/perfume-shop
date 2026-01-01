package com.abed.perfumeshop.order.controller;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.order.dto.CreateGuestOrderRequest;
import com.abed.perfumeshop.order.dto.GuestOrderResponseDTO;
import com.abed.perfumeshop.order.service.GuestOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/guest-orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    public ResponseEntity<Response<GuestOrderResponseDTO>> createOrder(@RequestBody @Valid CreateGuestOrderRequest createGuestOrderRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guestOrderService.createGuestOrder(createGuestOrderRequest));
    }

}
