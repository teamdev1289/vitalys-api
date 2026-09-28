package com.vitalys.modules.method.service;

import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.entity.MethodStepItem;
import com.vitalys.modules.method.entity.MethodValidationProtocol;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.method.repository.MethodStepItemRepository;
import com.vitalys.modules.method.repository.MethodValidationProtocolRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing Standard Operating Procedures (SOPs), testing methods,
 * execution steps, and method validation protocols according to ICH Q2(R1) and 21 CFR Part 11.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MethodService {

    private final MethodRepository methodRepository;
    private final MethodStepItemRepository stepItemRepository;
    private final MethodValidationProtocolRepository validationProtocolRepository;

    // ── Query Methods ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MethodResponse> getMethods(String keyword, String category, String validationStatus) {
        List<Method> methods = methodRepository.searchMethods(
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                category != null && !category.isBlank() ? category.trim() : null,
                validationStatus != null && !validationStatus.isBlank() ? validationStatus.trim() : null
        );

        Map<Long, Integer> stepCounts = methods.stream().collect(Collectors.toMap(
                Method::getId,
                m -> stepItemRepository.findByMethodIdOrderByOrderIndexAsc(m.getId()).size()
        ));

        return methods.stream()
                .map(m -> toResponse(m, stepCounts.getOrDefault(m.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public MethodDetailResponse getMethodById(Long id) {
        Method method = findMethodOrThrow(id);
        List<MethodStepItem> steps = stepItemRepository.findByMethodIdOrderByOrderIndexAsc(id);
        List<MethodValidationProtocol> protocols = validationProtocolRepository.findByMethodId(id);

        return toDetailResponse(method, steps, protocols);
    }

    @Transactional(readOnly = true)
    public MethodDetailResponse getMethodByCode(String methodCode) {
        Method method = methodRepository.findByMethodCode(methodCode)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phương pháp thử với mã: " + methodCode));
        List<MethodStepItem> steps = stepItemRepository.findByMethodIdOrderByOrderIndexAsc(method.getId());
        List<MethodValidationProtocol> protocols = validationProtocolRepository.findByMethodId(method.getId());

        return toDetailResponse(method, steps, protocols);
    }

    // ── Mutation Methods ─────────────────────────────────────────────────────

    @Auditable(module = "METHOD", entity = "Method")
    @Transactional
    public MethodDetailResponse createMethod(MethodCreateRequest request) {
        if (methodRepository.findByMethodCode(request.getMethodCode()).isPresent()) {
            throw new IllegalArgumentException("Mã phương pháp thử '" + request.getMethodCode() + "' đã tồn tại");
        }

        Method method = Method.builder()
                .methodCode(request.getMethodCode().trim().toUpperCase())
                .name(request.getName().trim())
                .version(request.getVersion().trim())
                .category(request.getCategory() != null ? request.getCategory() : "ASSAY")
                .instrumentType(request.getInstrumentType())
                .sourceStandard(request.getSourceStandard())
                .validationStatus("DRAFT")
                .isActive(true)
                .description(request.getDescription())
                .bodyTemplate(request.getBodyTemplate())
                .departmentId(request.getDepartmentId())
                .effectiveDate(request.getEffectiveDate())
                .reviewDueDate(request.getReviewDueDate())
                .build();

        Method saved = methodRepository.save(method);
        log.info("Created new testing method: {} ({})", saved.getMethodCode(), saved.getName());

        List<MethodStepItem> savedSteps = new ArrayList<>();
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            int idx = 1;
            for (MethodStepRequest stepReq : request.getSteps()) {
                MethodStepItem step = MethodStepItem.builder()
                        .methodId(saved.getId())
                        .stepKey(stepReq.getStepKey())
                        .label(stepReq.getLabel())
                        .role(stepReq.getRole() != null ? stepReq.getRole() : "ANALYST")
                        .expectedItemId(stepReq.getExpectedItemId())
                        .expectedQuantity(stepReq.getExpectedQuantity())
                        .unit(stepReq.getUnit())
                        .orderIndex(stepReq.getOrderIndex() != null ? stepReq.getOrderIndex() : String.valueOf(idx++))
                        .description(stepReq.getDescription())
                        .instructionNotes(stepReq.getInstructionNotes())
                        .build();
                savedSteps.add(stepItemRepository.save(step));
            }
        }

        return toDetailResponse(saved, savedSteps, List.of());
    }

    @Auditable(module = "METHOD", entity = "Method")
    @Transactional
    public MethodResponse updateMethod(Long id, MethodUpdateRequest request) {
        Method method = findMethodOrThrow(id);

        method.setName(request.getName().trim());
        method.setVersion(request.getVersion().trim());
        if (request.getCategory() != null) method.setCategory(request.getCategory());
        if (request.getInstrumentType() != null) method.setInstrumentType(request.getInstrumentType());
        if (request.getSourceStandard() != null) method.setSourceStandard(request.getSourceStandard());
        if (request.getDescription() != null) method.setDescription(request.getDescription());
        if (request.getBodyTemplate() != null) method.setBodyTemplate(request.getBodyTemplate());
        if (request.getDepartmentId() != null) method.setDepartmentId(request.getDepartmentId());
        if (request.getIsActive() != null) method.setIsActive(request.getIsActive());
        if (request.getEffectiveDate() != null) method.setEffectiveDate(request.getEffectiveDate());
        if (request.getReviewDueDate() != null) method.setReviewDueDate(request.getReviewDueDate());

        Method updated = methodRepository.save(method);
        int stepCount = stepItemRepository.findByMethodIdOrderByOrderIndexAsc(id).size();
        return toResponse(updated, stepCount);
    }

    @Auditable(module = "METHOD", entity = "Method")
    @Transactional
    public MethodResponse updateMethodStatus(Long id, MethodStatusUpdateRequest request) {
        Method method = findMethodOrThrow(id);
        String targetStatus = request.getStatus().trim().toUpperCase();

        // Valid statuses: DRAFT, VALIDATED, RELEASED, DEPRECATED
        validateStatusTransition(method.getValidationStatus(), targetStatus);

        method.setValidationStatus(targetStatus);
        if ("RELEASED".equals(targetStatus) && method.getEffectiveDate() == null) {
            method.setEffectiveDate(OffsetDateTime.now());
        }
        if ("DEPRECATED".equals(targetStatus)) {
            method.setIsActive(false);
        }

        Method updated = methodRepository.save(method);
        log.info("Method {} transitioned from {} to {}", method.getMethodCode(), method.getValidationStatus(), targetStatus);
        int stepCount = stepItemRepository.findByMethodIdOrderByOrderIndexAsc(id).size();
        return toResponse(updated, stepCount);
    }

    @Auditable(module = "METHOD", entity = "Method")
    @Transactional
    public void deleteMethod(Long id) {
        Method method = findMethodOrThrow(id);
        if ("RELEASED".equalsIgnoreCase(method.getValidationStatus())) {
            // Under GxP/21 CFR Part 11, released SOPs cannot be deleted physically
            method.setIsActive(false);
            method.setValidationStatus("DEPRECATED");
            methodRepository.save(method);
            log.warn("Method {} was released and has been deprecated instead of hard deleted", method.getMethodCode());
            return;
        }

        stepItemRepository.deleteByMethodId(id);
        methodRepository.delete(method);
        log.info("Deleted draft testing method: {}", method.getMethodCode());
    }

    // ── Steps Management ─────────────────────────────────────────────────────

    @Auditable(module = "METHOD", entity = "MethodStepItem")
    @Transactional
    public List<MethodStepResponse> updateMethodSteps(Long methodId, List<MethodStepRequest> steps) {
        findMethodOrThrow(methodId);
        stepItemRepository.deleteByMethodId(methodId);

        List<MethodStepItem> savedSteps = new ArrayList<>();
        int idx = 1;
        for (MethodStepRequest stepReq : steps) {
            MethodStepItem item = MethodStepItem.builder()
                    .methodId(methodId)
                    .stepKey(stepReq.getStepKey())
                    .label(stepReq.getLabel())
                    .role(stepReq.getRole() != null ? stepReq.getRole() : "ANALYST")
                    .expectedItemId(stepReq.getExpectedItemId())
                    .expectedQuantity(stepReq.getExpectedQuantity())
                    .unit(stepReq.getUnit())
                    .orderIndex(stepReq.getOrderIndex() != null ? stepReq.getOrderIndex() : String.valueOf(idx++))
                    .description(stepReq.getDescription())
                    .instructionNotes(stepReq.getInstructionNotes())
                    .build();
            savedSteps.add(stepItemRepository.save(item));
        }

        return savedSteps.stream().map(this::toStepResponse).toList();
    }

    // ── Validation Protocols ─────────────────────────────────────────────────

    @Auditable(module = "METHOD", entity = "MethodValidationProtocol")
    @Transactional
    public MethodValidationProtocolResponse addValidationProtocol(Long methodId, MethodValidationProtocolRequest request) {
        Method method = findMethodOrThrow(methodId);

        if (validationProtocolRepository.findByProtocolCode(request.getProtocolCode()).isPresent()) {
            throw new IllegalArgumentException("Mã đề cương thẩm định '" + request.getProtocolCode() + "' đã tồn tại");
        }

        MethodValidationProtocol protocol = MethodValidationProtocol.builder()
                .methodId(methodId)
                .protocolCode(request.getProtocolCode().trim())
                .title(request.getTitle().trim())
                .validationType(request.getValidationType() != null ? request.getValidationType() : "FULL_VALIDATION")
                .triggerReason(request.getTriggerReason())
                .status(request.getStatus() != null ? request.getStatus() : "IN_PROGRESS")
                .approvedBy(request.getApprovedBy())
                .approvedDate(request.getApprovedDate())
                .description(request.getDescription())
                .conclusion(request.getConclusion())
                .build();

        MethodValidationProtocol saved = validationProtocolRepository.save(protocol);

        // If validation protocol is approved, auto-transition method to VALIDATED if currently DRAFT
        if ("APPROVED".equalsIgnoreCase(saved.getStatus()) && "DRAFT".equalsIgnoreCase(method.getValidationStatus())) {
            method.setValidationStatus("VALIDATED");
            methodRepository.save(method);
        }

        return toProtocolResponse(saved);
    }

    // ── Helpers & Mappers ────────────────────────────────────────────────────

    private Method findMethodOrThrow(Long id) {
        return methodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phương pháp thử với ID: " + id));
    }

    private void validateStatusTransition(String current, String target) {
        if (current.equalsIgnoreCase(target)) return;

        boolean allowed = switch (current) {
            case "DRAFT" -> target.equals("VALIDATED") || target.equals("DEPRECATED");
            case "VALIDATED" -> target.equals("RELEASED") || target.equals("DRAFT") || target.equals("DEPRECATED");
            case "RELEASED" -> target.equals("DEPRECATED");
            case "DEPRECATED" -> false;
            default -> true;
        };

        if (!allowed) {
            throw new IllegalStateException("Không được phép chuyển đổi trạng thái phương pháp thử từ " + current + " sang " + target);
        }
    }

    private MethodResponse toResponse(Method m, int stepCount) {
        return MethodResponse.builder()
                .id(m.getId())
                .departmentId(m.getDepartmentId())
                .methodCode(m.getMethodCode())
                .name(m.getName())
                .version(m.getVersion())
                .category(m.getCategory())
                .instrumentType(m.getInstrumentType())
                .sourceStandard(m.getSourceStandard())
                .validationStatus(m.getValidationStatus())
                .isActive(m.getIsActive())
                .effectiveDate(m.getEffectiveDate())
                .reviewDueDate(m.getReviewDueDate())
                .description(m.getDescription())
                .bodyTemplate(m.getBodyTemplate())
                .stepCount(stepCount)
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private MethodDetailResponse toDetailResponse(Method m, List<MethodStepItem> steps, List<MethodValidationProtocol> protocols) {
        return MethodDetailResponse.builder()
                .id(m.getId())
                .departmentId(m.getDepartmentId())
                .methodCode(m.getMethodCode())
                .name(m.getName())
                .version(m.getVersion())
                .category(m.getCategory())
                .instrumentType(m.getInstrumentType())
                .sourceStandard(m.getSourceStandard())
                .validationStatus(m.getValidationStatus())
                .isActive(m.getIsActive())
                .effectiveDate(m.getEffectiveDate())
                .reviewDueDate(m.getReviewDueDate())
                .description(m.getDescription())
                .bodyTemplate(m.getBodyTemplate())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .steps(steps.stream().map(this::toStepResponse).toList())
                .validationProtocols(protocols.stream().map(this::toProtocolResponse).toList())
                .build();
    }

    private MethodStepResponse toStepResponse(MethodStepItem item) {
        return MethodStepResponse.builder()
                .id(item.getId())
                .methodId(item.getMethodId())
                .stepKey(item.getStepKey())
                .label(item.getLabel())
                .role(item.getRole())
                .expectedItemId(item.getExpectedItemId())
                .expectedQuantity(item.getExpectedQuantity())
                .unit(item.getUnit())
                .orderIndex(item.getOrderIndex())
                .description(item.getDescription())
                .instructionNotes(item.getInstructionNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private MethodValidationProtocolResponse toProtocolResponse(MethodValidationProtocol p) {
        return MethodValidationProtocolResponse.builder()
                .id(p.getId())
                .methodId(p.getMethodId())
                .protocolCode(p.getProtocolCode())
                .title(p.getTitle())
                .validationType(p.getValidationType())
                .triggerReason(p.getTriggerReason())
                .status(p.getStatus())
                .approvedBy(p.getApprovedBy())
                .approvedDate(p.getApprovedDate())
                .description(p.getDescription())
                .conclusion(p.getConclusion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
