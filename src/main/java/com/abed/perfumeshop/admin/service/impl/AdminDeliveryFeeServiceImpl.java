package com.abed.perfumeshop.admin.service.impl;

import com.abed.perfumeshop.admin.service.AdminDeliveryFeeService;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;
import com.abed.perfumeshop.delivery.dto.UpdateDeliveryFeeRequest;
import com.abed.perfumeshop.delivery.entity.DeliveryFee;
import com.abed.perfumeshop.delivery.repo.DeliveryFeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDeliveryFeeServiceImpl implements AdminDeliveryFeeService {

    private final DeliveryFeeRepo deliveryFeeRepo;

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryFeeDTO> getAllDeliveryFees() {
        return deliveryFeeRepo.findAllByOrderByGovernorateAsc()
                .stream()
                .map(fee -> DeliveryFeeDTO.builder()
                        .id(fee.getId())
                        .governorate(fee.getGovernorate())
                        .shippingFee(fee.getShippingFee())
                        .active(fee.getActive())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateDeliveryFee(Long deliveryFeeId, UpdateDeliveryFeeRequest updateDeliveryFeeRequest) {
        DeliveryFee deliveryFee = deliveryFeeRepo.findById(deliveryFeeId)
                .orElseThrow(() -> new NotFoundException("delivery.fee.not.found"));

        deliveryFee.setShippingFee(updateDeliveryFeeRequest.getShippingFee());
        deliveryFee.setActive(updateDeliveryFeeRequest.getActive());

        deliveryFeeRepo.save(deliveryFee);
    }

}
