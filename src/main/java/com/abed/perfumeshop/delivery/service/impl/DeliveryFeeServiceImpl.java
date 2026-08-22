package com.abed.perfumeshop.delivery.service.impl;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;
import com.abed.perfumeshop.delivery.repo.DeliveryFeeRepo;
import com.abed.perfumeshop.delivery.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryFeeServiceImpl implements DeliveryFeeService {

    private final DeliveryFeeRepo deliveryFeeRepo;

    @Override
    public BigDecimal getShippingFee(Governorate governorate) {
        return deliveryFeeRepo.findByGovernorateAndActiveTrue(governorate)
                .orElseThrow(() -> new NotFoundException("delivery.fee.not.found"))
                .getShippingFee();
    }

    @Override
    public List<DeliveryFeeDTO> getActiveDeliveryFees() {
        return deliveryFeeRepo.findByActiveTrueOrderByGovernorateAsc()
                .stream()
                .map(deliveryFee -> DeliveryFeeDTO.builder()
                        .governorate(deliveryFee.getGovernorate())
                        .shippingFee(deliveryFee.getShippingFee())
                        .build())
                .toList();
    }

}
