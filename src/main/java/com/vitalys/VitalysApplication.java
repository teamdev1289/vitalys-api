package com.vitalys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Vitalys Vitalys - Main Application Entry Point.
 * Laboratory Information Management System backend API.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "dateTimeProvider")
@EnableAsync
public class VitalysApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitalysApplication.class, args);
    }
}
