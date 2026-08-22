package com.abed.perfumeshop.seed;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.common.exception.SeedConfigException;
import com.abed.perfumeshop.delivery.entity.DeliveryFee;
import com.abed.perfumeshop.delivery.repo.DeliveryFeeRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(value = "data.initialization")
public class DataInitializer implements CommandLineRunner {

    private final AdminRepo adminRepo;
    private final DeliveryFeeRepo deliveryFeeRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.firstName}")
    private String firstName;

    @Value("${admin.lastName}")
    private String lastName;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String @NonNull... args) {
        if (adminEmail == null || adminPassword == null) {
            throw new SeedConfigException("seed.admin.credentials.not.configured");
        }

        createAdminUserIfNotExists();
        createDeliveryFeesIfNotExists();
    }

    // ========== Private Helper Methods ==========
    private void createAdminUserIfNotExists() {
        if (adminRepo.existsByEmail(adminEmail)) {
            return;
        }

        Admin admin = Admin.builder()
                .firstName(new String(Base64.getDecoder().decode(firstName)))
                .lastName(new String(Base64.getDecoder().decode(lastName)))
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .build();

        adminRepo.save(admin);
    }

    private void createDeliveryFeesIfNotExists() {
        Map<Governorate, BigDecimal> shippingFees = Map.ofEntries(
                Map.entry(Governorate.AMMAN, new BigDecimal("2.00")),
                Map.entry(Governorate.ZARQA, new BigDecimal("3.00")),
                Map.entry(Governorate.IRBID, new BigDecimal("4.00")),
                Map.entry(Governorate.BALQA, new BigDecimal("4.00")),
                Map.entry(Governorate.MADABA, new BigDecimal("4.00")),
                Map.entry(Governorate.KARAK, new BigDecimal("5.00")),
                Map.entry(Governorate.JERASH, new BigDecimal("5.00")),
                Map.entry(Governorate.AJLOUN, new BigDecimal("5.00")),
                Map.entry(Governorate.MAFRAQ, new BigDecimal("5.00")),
                Map.entry(Governorate.TAFILAH, new BigDecimal("6.00")),
                Map.entry(Governorate.MAAN, new BigDecimal("6.00")),
                Map.entry(Governorate.AQABA, new BigDecimal("6.00"))
        );

        List<DeliveryFee> missingDeliveryFees = shippingFees.entrySet().stream()
                .filter(entry -> !deliveryFeeRepo.existsByGovernorate(entry.getKey()))
                .map(entry -> DeliveryFee.builder()
                        .governorate(entry.getKey())
                        .shippingFee(entry.getValue())
                        .active(true)
                        .build())
                .toList();

        deliveryFeeRepo.saveAll(missingDeliveryFees);
    }

}
