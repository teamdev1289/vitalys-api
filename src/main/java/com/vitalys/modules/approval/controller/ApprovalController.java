package com.vitalys.modules.approval.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.approval.dto.ApprovalActionRequest;
import com.vitalys.modules.approval.dto.ApprovalStepResponse;
import com.vitalys.modules.approval.dto.WorkflowResponse;
import com.vitalys.modules.approval.service.ApprovalWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval & 21 CFR Part 11 Signatures", description = "Multi-stage approval workflow and compliant electronic signatures")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {

    private final ApprovalWorkflowService workflowService;

    @Operation(summary = "Get multi-stage approval workflow for an entity (e.g. SAMPLE, REPORT)")
    @GetMapping("/workflows")
    @PreAuthorize("hasAuthority('APPROVAL:WORKFLOW:READ')")
    public ResponseEntity<ResponseDto<WorkflowResponse>> getWorkflow(
            @RequestParam(defaultValue = "SAMPLE") String entityType,
            @RequestParam Long entityId
    ) {
        WorkflowResponse response = workflowService.getWorkflow(entityType, entityId);
        return ResponseEntity.ok(ResponseDto.ok(response));
    }

    @Operation(summary = "Execute 21 CFR Part 11 Electronic Signature action on a workflow step")
    @PostMapping("/steps/{stepId}/action")
    @PreAuthorize("hasAuthority('APPROVAL:SIGN:EXECUTE')")
    public ResponseEntity<ResponseDto<ApprovalStepResponse>> actionStep(
            @PathVariable Long stepId,
            @Valid @RequestBody ApprovalActionRequest request
    ) {
        ApprovalStepResponse response = workflowService.actionStep(stepId, request);
        return ResponseEntity.ok(ResponseDto.ok("Thực hiện ký điện tử thành công (21 CFR Part 11)", response));
    }
}
