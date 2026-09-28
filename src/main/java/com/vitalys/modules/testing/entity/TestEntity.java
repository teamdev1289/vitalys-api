package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Test entity representing an analytical procedure performed on a sample.
 */
@Entity
@Table(name = "testing_test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_code", unique = true, length = 100)
    private String testCode;

    @Column(name = "sample_id", nullable = false)
    private Long sampleId;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "spec_item_id")
    private Long specItemId;

    @Column(name = "run_id")
    private Long runId;

    @Column(name = "form_template_id")
    private Long formTemplateId;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "priority", length = 50)
    private String priority; // NORMAL, URGENT, EMERGENCY

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "status", length = 50)
    private String status; // ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
