package com.abed.perfumeshop.admin.controller;

import com.abed.perfumeshop.admin.service.AdminDeliveryFeeService;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;
import com.abed.perfumeshop.delivery.dto.UpdateDeliveryFeeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/delivery-fees")
@RequiredArgsConstructor
public class AdminDeliveryFeeController {

    private final AdminDeliveryFeeService adminDeliveryFeeService;

    @GetMapping
    public ResponseEntity<Response<List<DeliveryFeeDTO>>> getAllDeliveryFees() {
        return ResponseEntity.ok(
                Response.<List<DeliveryFeeDTO>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("delivery.fees.retrieved.successfully")
                        .data(adminDeliveryFeeService.getAllDeliveryFees())
                        .build()
        );
    }

    @PutMapping("/{deliveryFeeId}")
    public ResponseEntity<Response<Void>> updateDeliveryFee(
            @PathVariable Long deliveryFeeId,
            @RequestBody @Valid UpdateDeliveryFeeRequest updateDeliveryFeeRequest
    ) {
        adminDeliveryFeeService.updateDeliveryFee(deliveryFeeId, updateDeliveryFeeRequest);

        return ResponseEntity.ok(
                Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("delivery.fee.updated.successfully")
                        .build()
        );
    }

}
