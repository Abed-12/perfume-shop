package com.abed.perfumeshop.seed;

import com.abed.perfumeshop.admin.entity.Admin;
import com.abed.perfumeshop.admin.repo.AdminRepo;
import com.abed.perfumeshop.common.exception.SeedConfigException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(value = "data.initialization")
public class DataInitializer implements CommandLineRunner {

    private final AdminRepo adminRepo;
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

}

