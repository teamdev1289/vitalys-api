package com.vitalys.modules.sys.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * System event log for login/logout, security events, and operational actions.
 * Stores IP address and user agent for security analysis.
 */
@Entity
@Table(name = "sys_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Result status: SUCCESS or FAILED.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;
}
