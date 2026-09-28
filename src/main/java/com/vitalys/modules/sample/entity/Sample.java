package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Physical sample instance logged into the laboratory.
 * Supports accessioning, barcoding, storage condition tracking, and custody.
 */
@Entity
@Table(name = "sample_sample")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "spec_set_id")
    private Long specSetId;

    @Column(name = "sample_code", unique = true, length = 100)
    private String sampleCode;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "status", length = 50)
    private String status; // SUBMITTED, RECEIVED, ASSIGNED, TESTING, REVIEWED, APPROVED, REJECTED, DISPOSED

    @Column(name = "storage_condition", length = 100)
    private String storageCondition; // AMBIENT_15_25C, COLD_2_8C, FREEZER_MINUS_20C, FREEZER_MINUS_80C, DESICCATED

    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit", length = 50)
    private String unit; // TABLETS, CAPSULES, AMPOULES, VIALS, ML, GRAMS

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "sampling_date")
    private OffsetDateTime samplingDate;

    @Column(name = "sampling_location", length = 255)
    private String samplingLocation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
