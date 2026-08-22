package com.abed.perfumeshop.delivery.repo;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.delivery.entity.DeliveryFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryFeeRepo extends JpaRepository<DeliveryFee, Long> {

    boolean existsByGovernorate(Governorate governorate);

    Optional<DeliveryFee> findByGovernorateAndActiveTrue(Governorate governorate);

    List<DeliveryFee> findByActiveTrueOrderByGovernorateAsc();

}
