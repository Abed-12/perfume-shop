package com.abed.perfumeshop.order.helper;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OrderNumberGenerator {

    public String generate(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 4)
                .toUpperCase();

        return prefix + "-" + datePart + timePart + randomPart;
    }
}
