package com.vitalys.modules.sys.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Immutable audit trail record.
 * Cannot be modified or deleted — enforced by database trigger and service layer.
 * Stores old/new values as JSONB for full change history.
 */
@Entity
@Table(name = "sys_audit_trail")
@org.hibernate.annotations.Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    /**
     * Action: CREATE, UPDATE, or DELETE.
     */
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 50)
    private String performedBy;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Previous state of the entity serialized as JSONB.
     * Null for CREATE operations.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    /**
     * New state of the entity serialized as JSONB.
     * Null for DELETE operations.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    // Intentionally no setters — audit records are immutable after creation
}
