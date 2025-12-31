package com.abed.perfumeshop.passwordResetCode.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Function;

@Component
public class CodeGenerator {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateUniqueCode(Function<String, Boolean> existsCheck) {
        String code;
        do {
            code = generateRandomCode();
        } while (existsCheck.apply(code));

        return code;
    }

    // ========== Private Helper Methods ==========
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }

        return sb.toString();
    }

}
