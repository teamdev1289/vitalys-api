package com.vitalys.modules.stability.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Sample pull event scheduled at a specific time point and storage condition
 */
@Entity
@Table(name = "stability_pull_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityPullEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "time_point_id", nullable = false)
    private Long timePointId;

    @Column(name = "storage_condition_id", nullable = false)
    private Long storageConditionId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "window_start")
    private LocalDate windowStart;

    @Column(name = "window_end")
    private LocalDate windowEnd;

    @Column(name = "pull_date")
    private OffsetDateTime pullDate;

    @Column(name = "pulled_by")
    private String pulledBy;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "test_request_id")
    private Long testRequestId;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "SCHEDULED"; // SCHEDULED, PENDING_PULL, PULLED, IN_TESTING, COMPLETED, CANCELLED

    @Column(name = "assay_result")
    private Double assayResult;

    @Column(name = "dissolution_result")
    private Double dissolutionResult;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
