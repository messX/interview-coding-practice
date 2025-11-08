package com.interview.practice.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class InventoryReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryReservationApplication.class, args);
    }
}

