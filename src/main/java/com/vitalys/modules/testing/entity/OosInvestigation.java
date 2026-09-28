package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Out of Specification (OOS) lab investigation entity.
 * Supports FDA / MHRA Phase 1 Lab Investigation workflow.
 */
@Entity
@Table(name = "testing_oos_investigation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OosInvestigation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investigation_code", unique = true, nullable = false, length = 100)
    private String investigationCode;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    @Column(name = "sample_id", nullable = false)
    private Long sampleId;

    @Column(name = "phase", nullable = false, length = 50)
    private String phase; // PHASE_1_LAB, PHASE_2_MFG, CLOSED

    @Column(name = "root_cause_category", length = 100)
    private String rootCauseCategory; // ANALYST_ERROR, INSTRUMENT_ERROR, SAMPLE_PREPARATION, BATCH_FAILURE, INCONCLUSIVE

    @Column(name = "immediate_action", columnDefinition = "TEXT")
    private String immediateAction;

    @Column(name = "investigation_findings", columnDefinition = "TEXT")
    private String investigationFindings;

    @Column(name = "investigated_by", nullable = false, length = 100)
    private String investigatedBy;

    @Column(name = "investigated_at", nullable = false)
    private OffsetDateTime investigatedAt;

    @Column(name = "supervisor_reviewed_by", length = 100)
    private String supervisorReviewedBy;

    @Column(name = "supervisor_comments", columnDefinition = "TEXT")
    private String supervisorComments;

    @Column(name = "retest_approved")
    private Boolean retestApproved;

    @Column(name = "conclusion", length = 50)
    private String conclusion; // CONFIRMED_OOS, LAB_ERROR_INVALIDATED, RETEST_PASSED, INCONCLUSIVE

    @Column(name = "status", nullable = false, length = 50)
    private String status; // OPEN, UNDER_REVIEW, CLOSED
}
