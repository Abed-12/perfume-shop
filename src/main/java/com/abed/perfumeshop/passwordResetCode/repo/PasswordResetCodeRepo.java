package com.abed.perfumeshop.passwordResetCode.repo;

import com.abed.perfumeshop.common.enums.UserType;
import com.abed.perfumeshop.passwordResetCode.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepo extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findByCode(String code);

    void deleteByUserIdAndUserType(Long userId, UserType userType);

}
