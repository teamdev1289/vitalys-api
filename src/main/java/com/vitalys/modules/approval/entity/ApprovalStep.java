package com.vitalys.modules.approval.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Generic approval workflow step
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

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "step_order")
    private String stepOrder;

    @Column(name = "required_role")
    private String requiredRole;

    @Column(name = "status")
    private String status;

    @Column(name = "actioned_by")
    private String actionedBy;

    @Column(name = "actioned_at")
    private OffsetDateTime actionedAt;

    @Column(name = "comment")
    private String comment;

    @Column(name = "e_signature_hash")
    private String eSignatureHash;


}
