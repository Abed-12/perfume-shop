package com.abed.perfumeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PerfumeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerfumeShopApplication.class, args);
    }

}
