package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Testing Request entity representing formal lab analysis orders
 * from manufacturing batches, R&D formulation trials, or external clients.
 */
@Entity
@Table(name = "sample_test_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", unique = true, length = 100)
    private String requestCode;

    @Column(name = "source_type", length = 50)
    private String sourceType; // BATCH, CUSTOMER, FORMULATION_TRIAL, STABILITY, OTHER

    @Column(name = "sample_type", length = 50)
    private String sampleType; // FINISHED_PRODUCT, RAW_MATERIAL, IN_PROCESS, STABILITY, ENVIRONMENTAL

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "process_stage")
    private String processStage;

    @Column(name = "formulation_trial_id")
    private Long formulationTrialId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "spec_set_id")
    private Long specSetId;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "request_date")
    private OffsetDateTime requestDate;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "priority", length = 20)
    private String priority; // NORMAL, URGENT, EMERGENCY

    @Column(name = "test_scope", length = 100)
    private String testScope; // RELEASE_TESTING, STABILITY_PULL_TESTING, FULL_COMPENDIAL, IDENTITY_ONLY, INVESTIGATION

    @Column(name = "status", length = 50)
    private String status; // SUBMITTED, RECEIVED, IN_TESTING, COMPLETED, CANCELLED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
