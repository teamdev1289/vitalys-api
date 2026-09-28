package com.vitalys.modules.approval.service;

import jakarta.persistence.EntityNotFoundException;
import com.vitalys.modules.approval.dto.ApprovalActionRequest;
import com.vitalys.modules.approval.dto.ApprovalStepResponse;
import com.vitalys.modules.approval.dto.WorkflowResponse;
import com.vitalys.modules.approval.entity.ApprovalStep;
import com.vitalys.modules.approval.entity.Report;
import com.vitalys.modules.approval.repository.ApprovalStepRepository;
import com.vitalys.modules.approval.repository.ReportRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Multi-stage Approval Workflow Engine complying with GxP and 21 CFR Part 11.
 * Supports 3 sequential stages:
 * Stage 1: Analyst Verification (OPERATOR)
 * Stage 2: Supervisor Technical Review (SUPERVISOR)
 * Stage 3: QA Final Approval (LAB_ADMIN / MANAGER)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalWorkflowService {

    private final ApprovalStepRepository stepRepository;
    private final ReportRepository reportRepository;
    private final SampleRepository sampleRepository;
    private final ESignatureService eSignatureService;
    private final SysAuditTrailRepository auditTrailRepository;

    /**
     * Retrieves or lazily initializes the multi-stage approval workflow for an entity.
     */
    @Transactional
    public WorkflowResponse getWorkflow(String entityType, Long entityId) {
        String type = entityType.toUpperCase();
        List<ApprovalStep> steps = stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc(type, entityId);

        // If it's a SAMPLE and steps do not exist yet, initialize standard 3-stage workflow
        if (steps.isEmpty() && "SAMPLE".equals(type)) {
            steps = initDefaultSampleWorkflow(entityId);
        }

        String currentUsername = getCurrentUsername();
        List<String> userRoles = getCurrentUserRoles();

        // Calculate overall workflow status
        String overallStatus = calculateOverallStatus(steps);

        // Find active step (first step that is PENDING)
        ApprovalStep currentStep = steps.stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);

        Integer currentStepNumber = currentStep != null ? currentStep.getStepNumber() : null;
        String currentRequiredRole = currentStep != null ? currentStep.getRequiredRole() : null;

        // Check if current logged-in user is authorized to sign the active step
        boolean canAction = false;
        if (currentStep != null) {
            canAction = userRoles.contains("ADMIN")
                    || userRoles.contains("IT_ADMIN")
                    || userRoles.contains("LAB_ADMIN")
                    || userRoles.contains(currentStep.getRequiredRole());
        }

        List<ApprovalStepResponse> stepResponses = steps.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return WorkflowResponse.builder()
                .entityType(type)
                .entityId(entityId)
                .overallStatus(overallStatus)
                .currentStepNumber(currentStepNumber)
                .currentRequiredRole(currentRequiredRole)
                .canCurrentUserAction(canAction)
                .steps(stepResponses)
                .build();
    }

    /**
     * Executes 21 CFR Part 11 Electronic Signature action (APPROVE or REJECT) on a workflow step.
     */
    @Transactional
    public ApprovalStepResponse actionStep(Long stepId, ApprovalActionRequest request) {
        String username = getCurrentUsername();
        OffsetDateTime now = OffsetDateTime.now();

        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ApprovalStep not found with id " + stepId));

        if (!"PENDING".equalsIgnoreCase(step.getStatus())) {
            throw new IllegalArgumentException("Bước phê duyệt #" + step.getStepNumber() + " hiện không ở trạng thái PENDING (Trạng thái hiện tại: " + step.getStatus() + ")");
        }

        // 1. Mandatory 21 CFR Part 11 re-authentication
        eSignatureService.verifySignerCredentials(username, request.getPassword());

        String action = request.getAction().toUpperCase();
        String meaning = request.getMeaning();
        if (meaning == null || meaning.isBlank()) {
            meaning = defaultMeaningForStep(step.getStepNumber(), action);
        }

        // 2. Cryptographic Digital Signature Hash (SHA-256)
        String signatureHash = eSignatureService.generateSignatureHash(username, step.getEntityId(), meaning, now);

        if ("APPROVE".equals(action)) {
            step.setStatus("APPROVED");
            step.setActionedBy(username);
            step.setActionedAt(now);
            step.setMeaning(meaning);
            step.setComment(request.getComment());
            step.setESignatureHash(signatureHash);
            stepRepository.save(step);

            log.info("21 CFR Part 11 Signature APPROVED by {} for step #{} (Entity: {} {})",
                    username, step.getStepNumber(), step.getEntityType(), step.getEntityId());

            // Advance workflow: find next step
            Optional<ApprovalStep> nextStepOpt = stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc(step.getEntityType(), step.getEntityId())
                    .stream()
                    .filter(s -> s.getStepNumber() == step.getStepNumber() + 1)
                    .findFirst();

            if (nextStepOpt.isPresent()) {
                ApprovalStep nextStep = nextStepOpt.get();
                nextStep.setStatus("PENDING");
                stepRepository.save(nextStep);
            } else {
                // All stages approved! Finalize entity state
                finalizeApproval(step.getEntityType(), step.getEntityId());
            }

        } else if ("REJECT".equals(action)) {
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("Lý do từ chối (comment) là bắt buộc theo quy định 21 CFR Part 11.");
            }

            step.setStatus("REJECTED");
            step.setActionedBy(username);
            step.setActionedAt(now);
            step.setMeaning(meaning);
            step.setComment(request.getComment());
            step.setESignatureHash(signatureHash);
            stepRepository.save(step);

            log.warn("21 CFR Part 11 Signature REJECTED by {} for step #{} (Entity: {} {})",
                    username, step.getStepNumber(), step.getEntityType(), step.getEntityId());

            // Reject remaining steps and entity
            finalizeRejection(step.getEntityType(), step.getEntityId(), step.getStepNumber());

        } else {
            throw new IllegalArgumentException("Invalid action: " + request.getAction() + ". Must be APPROVE or REJECT.");
        }

        // 3. Record in Audit Trail
        recordAudit(step, username, action, request.getComment(), signatureHash);

        return toResponse(step);
    }

    private void finalizeApproval(String entityType, Long entityId) {
        if ("SAMPLE".equalsIgnoreCase(entityType)) {
            sampleRepository.findById(entityId).ifPresent(sample -> {
                sample.setStatus("APPROVED");
                sampleRepository.save(sample);
            });

            reportRepository.findFirstBySampleIdAndStatusOrderByCreatedAtDesc(entityId, "DRAFT")
                    .ifPresent(report -> {
                        report.setStatus("APPROVED");
                        reportRepository.save(report);
                    });
        }
    }

    private void finalizeRejection(String entityType, Long entityId, Integer currentStepNumber) {
        // Skip subsequent steps
        List<ApprovalStep> steps = stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc(entityType, entityId);
        for (ApprovalStep s : steps) {
            if (s.getStepNumber() > currentStepNumber && !"APPROVED".equalsIgnoreCase(s.getStatus())) {
                s.setStatus("SKIPPED");
                stepRepository.save(s);
            }
        }

        if ("SAMPLE".equalsIgnoreCase(entityType)) {
            sampleRepository.findById(entityId).ifPresent(sample -> {
                sample.setStatus("REJECTED");
                sampleRepository.save(sample);
            });

            reportRepository.findFirstBySampleIdAndStatusOrderByCreatedAtDesc(entityId, "DRAFT")
                    .ifPresent(report -> {
                        report.setStatus("REJECTED");
                        reportRepository.save(report);
                    });
        }
    }

    private List<ApprovalStep> initDefaultSampleWorkflow(Long sampleId) {
        List<ApprovalStep> steps = new ArrayList<>();

        steps.add(ApprovalStep.builder()
                .entityType("SAMPLE")
                .entityId(sampleId)
                .stepNumber(1)
                .stepOrder("1")
                .stepName("Kiểm nghiệm viên (Analyst Verification)")
                .requiredRole("OPERATOR")
                .status("PENDING")
                .build());

        steps.add(ApprovalStep.builder()
                .entityType("SAMPLE")
                .entityId(sampleId)
                .stepNumber(2)
                .stepOrder("2")
                .stepName("Trưởng nhóm kỹ thuật (Supervisor Technical Review)")
                .requiredRole("SUPERVISOR")
                .status("WAITING")
                .build());

        steps.add(ApprovalStep.builder()
                .entityType("SAMPLE")
                .entityId(sampleId)
                .stepNumber(3)
                .stepOrder("3")
                .stepName("Trưởng phòng QA (QA Final Approval)")
                .requiredRole("LAB_ADMIN")
                .status("WAITING")
                .build());

        return stepRepository.saveAll(steps);
    }

    private String calculateOverallStatus(List<ApprovalStep> steps) {
        if (steps.isEmpty()) return "PENDING";

        boolean anyRejected = steps.stream().anyMatch(s -> "REJECTED".equalsIgnoreCase(s.getStatus()));
        if (anyRejected) return "REJECTED";

        boolean allApproved = steps.stream().allMatch(s -> "APPROVED".equalsIgnoreCase(s.getStatus()));
        if (allApproved) return "APPROVED";

        boolean anyApprovedOrPending = steps.stream().anyMatch(s ->
                "APPROVED".equalsIgnoreCase(s.getStatus()) || "PENDING".equalsIgnoreCase(s.getStatus()));
        if (anyApprovedOrPending) return "IN_PROGRESS";

        return "PENDING";
    }

    private String defaultMeaningForStep(Integer stepNumber, String action) {
        if ("REJECT".equalsIgnoreCase(action)) {
            return "Tôi từ chối xác nhận kết quả phân tích này do không đạt tiêu chuẩn kỹ thuật.";
        }
        if (stepNumber == null) return "Tôi phê duyệt tài liệu này.";
        return switch (stepNumber) {
            case 1 -> "Tôi xác nhận đã hoàn thành và kiểm tra tính toàn vẹn của dữ liệu phân tích (Analyst Verification).";
            case 2 -> "Tôi xác nhận đã rà soát kỹ thuật, thẩm định phương pháp và chấp thuận kết quả thử nghiệm (Supervisor Review).";
            case 3 -> "Tôi phê duyệt chính thức phát hành Phiếu Kiểm Nghiệm và xuất xưởng lô thuốc (QA Final Approval).";
            default -> "Tôi phê duyệt và ký điện tử tài liệu này.";
        };
    }

    private void recordAudit(ApprovalStep step, String username, String action, String comment, String signatureHash) {
        try {
            java.util.Map<String, Object> oldVal = java.util.Map.of(
                    "stepNumber", step.getStepNumber(),
                    "stepName", step.getStepName(),
                    "status", "PENDING"
            );
            java.util.Map<String, Object> newVal = new java.util.HashMap<>();
            newVal.put("status", step.getStatus());
            newVal.put("meaning", step.getMeaning() != null ? step.getMeaning() : "");
            newVal.put("hash", signatureHash != null ? signatureHash : "");
            newVal.put("comment", comment != null ? comment : "");

            SysAuditTrail audit = SysAuditTrail.builder()
                    .module("APPROVAL")
                    .entityName("ApprovalStep")
                    .entityId(String.valueOf(step.getId()))
                    .action(action.equals("APPROVE") ? "APPROVE_STEP" : "REJECT_STEP")
                    .performedBy(username)
                    .timestamp(OffsetDateTime.now())
                    .oldValue(oldVal)
                    .newValue(newVal)
                    .build();
            auditTrailRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to write audit trail for step action", e);
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private List<String> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return List.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toList());
    }

    private ApprovalStepResponse toResponse(ApprovalStep s) {
        return ApprovalStepResponse.builder()
                .id(s.getId())
                .entityType(s.getEntityType())
                .entityId(s.getEntityId())
                .stepNumber(s.getStepNumber())
                .stepName(s.getStepName())
                .requiredRole(s.getRequiredRole())
                .status(s.getStatus())
                .actionedBy(s.getActionedBy())
                .actionedAt(s.getActionedAt())
                .meaning(s.getMeaning())
                .comment(s.getComment())
                .eSignatureHash(s.getESignatureHash())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
