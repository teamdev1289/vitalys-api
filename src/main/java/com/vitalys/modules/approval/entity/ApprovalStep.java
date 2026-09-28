package com.vitalys.modules.approval.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Multi-stage approval workflow step with 21 CFR Part 11 Electronic Signature metadata.
 */
@Entity
@Table(name = "approval_approval_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // SAMPLE, REPORT, TEST

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "step_number")
    private Integer stepNumber; // 1, 2, 3

    @Column(name = "step_order")
    private String stepOrder; // "1", "2", "3"

    @Column(name = "step_name")
    private String stepName; // "Analyst Verification", "Supervisor Review", "QA Final Approval"

    @Column(name = "required_role")
    private String requiredRole; // OPERATOR, SUPERVISOR, LAB_ADMIN, MANAGER

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, SKIPPED

    @Column(name = "actioned_by")
    private String actionedBy;

    @Column(name = "actioned_at")
    private OffsetDateTime actionedAt;

    @Column(name = "meaning")
    private String meaning; // e.g. "I am the Author...", "I have Reviewed...", "I Approve..."

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "e_signature_hash")
    private String eSignatureHash; // SHA-256 hash of signer identity + timestamp + entity state
}
