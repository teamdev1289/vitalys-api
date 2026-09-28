package com.vitalys.modules.approval.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStepResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private Integer stepNumber;
    private String stepName;
    private String requiredRole;
    private String status;
    private String actionedBy;
    private OffsetDateTime actionedAt;
    private String meaning;
    private String comment;
    private String eSignatureHash;
    private OffsetDateTime createdAt;
}
