package com.vitalys.modules.stability.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Stability Study Protocol Header (ICH Q1A R2)
 */
@Entity
@Table(name = "stability_study")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityStudy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_code", unique = true, nullable = false)
    private String studyCode;

    @Column(name = "study_title", nullable = false)
    private String studyTitle;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "study_type", nullable = false)
    private String studyType; // LONG_TERM, ACCELERATED, INTERMEDIATE

    @Column(name = "protocol_number")
    private String protocolNumber;

    @Column(name = "duration_months", nullable = false)
    @Builder.Default
    private Integer durationMonths = 24;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // PLANNED, ACTIVE, COMPLETED, CANCELLED

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
