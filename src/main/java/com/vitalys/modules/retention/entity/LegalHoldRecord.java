package com.vitalys.modules.retention.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "legal_hold_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalHoldRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hold_code", unique = true, nullable = false, length = 50)
    private String holdCode; // e.g. LH-2026-FDA-001

    @Column(name = "case_title", nullable = false, length = 255)
    private String caseTitle;

    @Column(name = "case_reference", nullable = false, length = 100)
    private String caseReference;

    @Column(name = "target_module", nullable = false, length = 50)
    private String targetModule; // BATCH, SAMPLE, REPORT, SDMS

    @Column(name = "target_entity_id", nullable = false)
    private Long targetEntityId;

    @Column(name = "entity_code", length = 100)
    private String entityCode;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, RELEASED

    @Column(name = "placed_by", nullable = false, length = 100)
    private String placedBy;

    @Column(name = "placed_at", nullable = false)
    @Builder.Default
    private OffsetDateTime placedAt = OffsetDateTime.now();

    @Column(name = "released_by", length = 100)
    private String releasedBy;

    @Column(name = "released_at")
    private OffsetDateTime releasedAt;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
