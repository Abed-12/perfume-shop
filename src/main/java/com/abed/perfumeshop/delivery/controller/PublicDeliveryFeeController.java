package com.abed.perfumeshop.delivery.controller;

import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;
import com.abed.perfumeshop.delivery.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/delivery-fees")
@RequiredArgsConstructor
public class PublicDeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    @GetMapping
    public ResponseEntity<Response<List<DeliveryFeeDTO>>> getDeliveryFees() {
        return ResponseEntity.ok(
                Response.<List<DeliveryFeeDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("delivery.fees.retrieved.successfully")
                        .data(deliveryFeeService.getActiveDeliveryFees())
                        .build()
        );
    }

}
