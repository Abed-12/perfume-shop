package com.abed.perfumeshop.notification.repo;

import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepo extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserIdAndUserType(Long userId, UserType userType);

    List<DeviceToken> findByUserType(UserType userType);

    Optional<DeviceToken> findByToken(String token);

}
