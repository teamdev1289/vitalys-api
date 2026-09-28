package com.vitalys.modules.retention.service;

import java.util.NoSuchElementException;
import com.vitalys.modules.retention.dto.*;
import com.vitalys.modules.retention.entity.LegalHoldRecord;
import com.vitalys.modules.retention.entity.RetentionPolicy;
import com.vitalys.modules.retention.repository.LegalHoldRecordRepository;
import com.vitalys.modules.retention.repository.RetentionPolicyRepository;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionService {

    private final RetentionPolicyRepository retentionPolicyRepository;
    private final LegalHoldRecordRepository legalHoldRecordRepository;
    private final SysAuditTrailRepository auditTrailRepository;

    // ── Retention Policies ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RetentionPolicyResponse> getAllPolicies() {
        return retentionPolicyRepository.findAll().stream()
                .map(this::mapPolicyToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RetentionPolicyResponse updatePolicy(Long id, RetentionPolicyUpdateRequest request, String username) {
        RetentionPolicy policy = retentionPolicyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Retention Policy not found with ID: " + id));

        String oldVal = "Years: " + policy.getRetentionYears();
        policy.setRetentionYears(request.getRetentionYears());
        if (request.getIsPermanent() != null) policy.setIsPermanent(request.getIsPermanent());
        if (request.getDescription() != null) policy.setDescription(request.getDescription());
        if (request.getAutoArchive() != null) policy.setAutoArchive(request.getAutoArchive());

        RetentionPolicy updated = retentionPolicyRepository.save(policy);
        String newVal = "Years: " + updated.getRetentionYears();

        SysAuditTrail audit = SysAuditTrail.builder()
                .module("RETENTION")
                .entityName("RetentionPolicy")
                .entityId(updated.getId().toString())
                .action("UPDATE_POLICY")
                .oldValue(Map.of("years", oldVal))
                .newValue(Map.of("years", newVal))
                .performedBy(username != null ? username : "SYSTEM")
                .timestamp(OffsetDateTime.now())
                .build();
        auditTrailRepository.save(audit);

        log.info("Updated retention policy [{}]: retention = {} years", updated.getModuleName(), updated.getRetentionYears());
        return mapPolicyToResponse(updated);
    }

    // ── Legal Hold Management ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LegalHoldResponse> getLegalHolds(Pageable pageable) {
        return legalHoldRecordRepository.findAllByOrderByPlacedAtDesc(pageable)
                .map(this::mapHoldToResponse);
    }

    @Transactional
    public LegalHoldResponse placeLegalHold(LegalHoldCreateRequest request, String username) {
        String holdCode = "LH-" + OffsetDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        LegalHoldRecord hold = LegalHoldRecord.builder()
                .holdCode(holdCode)
                .caseTitle(request.getCaseTitle())
                .caseReference(request.getCaseReference())
                .targetModule(request.getTargetModule().toUpperCase())
                .targetEntityId(request.getTargetEntityId())
                .entityCode(request.getEntityCode())
                .status("ACTIVE")
                .placedBy(username != null ? username : (request.getPlacedBy() != null ? request.getPlacedBy() : "SYSTEM"))
                .placedAt(OffsetDateTime.now())
                .reason(request.getReason())
                .build();

        LegalHoldRecord saved = legalHoldRecordRepository.save(hold);

        SysAuditTrail audit = SysAuditTrail.builder()
                .module("RETENTION")
                .entityName("LegalHoldRecord")
                .entityId(saved.getId().toString())
                .action("PLACE_LEGAL_HOLD")
                .oldValue(Map.of("status", "NONE"))
                .newValue(Map.of("status", "ACTIVE", "holdCode", saved.getHoldCode(), "target", saved.getTargetModule() + ":" + saved.getTargetEntityId()))
                .performedBy(username != null ? username : "SYSTEM")
                .timestamp(OffsetDateTime.now())
                .build();
        auditTrailRepository.save(audit);

        log.warn("CRITICAL: Placed Legal Hold [{}] on {}:{} due to {}",
                saved.getHoldCode(), saved.getTargetModule(), saved.getTargetEntityId(), saved.getCaseTitle());

        return mapHoldToResponse(saved);
    }

    @Transactional
    public LegalHoldResponse releaseLegalHold(Long holdId, LegalHoldReleaseRequest request, String username) {
        LegalHoldRecord hold = legalHoldRecordRepository.findById(holdId)
                .orElseThrow(() -> new NoSuchElementException("Legal Hold record not found with ID: " + holdId));

        if ("RELEASED".equals(hold.getStatus())) {
            return mapHoldToResponse(hold);
        }

        hold.setStatus("RELEASED");
        hold.setReleasedBy(username != null ? username : (request.getReleasedBy() != null ? request.getReleasedBy() : "SYSTEM"));
        hold.setReleasedAt(OffsetDateTime.now());
        hold.setReason(hold.getReason() + " | [RELEASE NOTE: " + request.getReleaseNotes() + "]");

        LegalHoldRecord updated = legalHoldRecordRepository.save(hold);

        SysAuditTrail audit = SysAuditTrail.builder()
                .module("RETENTION")
                .entityName("LegalHoldRecord")
                .entityId(updated.getId().toString())
                .action("RELEASE_LEGAL_HOLD")
                .oldValue(Map.of("status", "ACTIVE"))
                .newValue(Map.of("status", "RELEASED", "holdCode", updated.getHoldCode()))
                .performedBy(username != null ? username : "SYSTEM")
                .timestamp(OffsetDateTime.now())
                .build();
        auditTrailRepository.save(audit);

        log.info("Released Legal Hold [{}] by user {}", updated.getHoldCode(), updated.getReleasedBy());
        return mapHoldToResponse(updated);
    }

    @Transactional(readOnly = true)
    public boolean isEntityHeld(String targetModule, Long targetEntityId) {
        return legalHoldRecordRepository.existsByTargetModuleAndTargetEntityIdAndStatus(
                targetModule.toUpperCase(), targetEntityId, "ACTIVE");
    }

    private RetentionPolicyResponse mapPolicyToResponse(RetentionPolicy p) {
        return RetentionPolicyResponse.builder()
                .id(p.getId())
                .moduleName(p.getModuleName())
                .categoryTitle(p.getCategoryTitle())
                .retentionYears(p.getRetentionYears())
                .isPermanent(p.getIsPermanent())
                .description(p.getDescription())
                .autoArchive(p.getAutoArchive())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private LegalHoldResponse mapHoldToResponse(LegalHoldRecord h) {
        return LegalHoldResponse.builder()
                .id(h.getId())
                .holdCode(h.getHoldCode())
                .caseTitle(h.getCaseTitle())
                .caseReference(h.getCaseReference())
                .targetModule(h.getTargetModule())
                .targetEntityId(h.getTargetEntityId())
                .entityCode(h.getEntityCode())
                .status(h.getStatus())
                .placedBy(h.getPlacedBy())
                .placedAt(h.getPlacedAt())
                .releasedBy(h.getReleasedBy())
                .releasedAt(h.getReleasedAt())
                .reason(h.getReason())
                .build();
    }
}
