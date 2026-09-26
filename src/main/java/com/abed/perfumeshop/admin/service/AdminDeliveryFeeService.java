package com.abed.perfumeshop.admin.service;

import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;
import com.abed.perfumeshop.delivery.dto.UpdateDeliveryFeeRequest;

import java.util.List;

public interface AdminDeliveryFeeService {

    List<DeliveryFeeDTO> getAllDeliveryFees();

    void updateDeliveryFee(Long deliveryFeeId, UpdateDeliveryFeeRequest updateDeliveryFeeRequest);

}
