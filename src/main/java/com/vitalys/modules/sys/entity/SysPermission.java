package com.vitalys.modules.sys.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Granular permission entry identified by module + screen + action triple.
 * Example: module=SYS, screen=USER, action=CREATE → SYS:USER:CREATE
 */
@Entity
@Table(name = "sys_permission",
       uniqueConstraints = @UniqueConstraint(name = "uq_perm", columnNames = {"module", "screen", "action"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Functional module, e.g., SYS, LAB, REPORT.
     */
    @Column(name = "module", nullable = false, length = 50)
    private String module;

    /**
     * Screen/feature within the module, e.g., USER, ROLE, AUDIT.
     */
    @Column(name = "screen", nullable = false, length = 50)
    private String screen;

    /**
     * Action performed, e.g., READ, CREATE, UPDATE, DELETE, EXPORT, VIEW_PRICE.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Returns the canonical permission key: MODULE:SCREEN:ACTION
     */
    public String toKey() {
        return module + ":" + screen + ":" + action;
    }
}
