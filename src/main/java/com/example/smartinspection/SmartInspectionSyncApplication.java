package com.example.smartinspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartInspectionSyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartInspectionSyncApplication.class, args);
    }
}
