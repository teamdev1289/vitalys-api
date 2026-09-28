package com.vitalys.modules.approval.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowResponse {
    private String entityType;
    private Long entityId;
    private String overallStatus; // PENDING, IN_PROGRESS, APPROVED, REJECTED
    private Integer currentStepNumber;
    private String currentRequiredRole;
    private boolean canCurrentUserAction;
    private List<ApprovalStepResponse> steps;
}
