package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Request for testing
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

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "sample_type")
    private String sampleType;

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

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "request_date")
    private OffsetDateTime requestDate;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "priority")
    private String priority;

    @Column(name = "test_scope")
    private String testScope;

    @Column(name = "status")
    private String status;


}
