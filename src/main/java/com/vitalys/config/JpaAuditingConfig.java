package com.vitalys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing configuration.
 * Provides the current authenticated username as the auditor for
 * @CreatedBy and @LastModifiedBy fields if added to entities.
 */
@Configuration
public class JpaAuditingConfig {

    /**
     * Returns the username of the currently authenticated user,
     * or "SYSTEM" during background/startup operations.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            return Optional.of(auth.getName());
        };
    }

    @Bean
    public org.springframework.data.auditing.DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(java.time.OffsetDateTime.now());
    }
}
