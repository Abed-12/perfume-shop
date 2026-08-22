package com.abed.perfumeshop.delivery.service;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.delivery.dto.DeliveryFeeDTO;

import java.math.BigDecimal;
import java.util.List;

public interface DeliveryFeeService {

    BigDecimal getShippingFee(Governorate governorate);

    List<DeliveryFeeDTO> getActiveDeliveryFees();

}
