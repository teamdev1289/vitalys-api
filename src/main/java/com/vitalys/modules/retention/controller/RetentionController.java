package com.vitalys.modules.retention.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.retention.dto.*;
import com.vitalys.modules.retention.service.RetentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/retention")
@RequiredArgsConstructor
@Tag(name = "Data Retention & Legal Hold", description = "21 CFR Part 11 Electronic Record Retention Policies & FDA Legal Hold Locks")
public class RetentionController {

    private final RetentionService retentionService;

    // ── Retention Policies ──────────────────────────────────────────────────

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('RETENTION:POLICY:READ') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Get list of data retention policies")
    public ResponseEntity<ResponseDto<List<RetentionPolicyResponse>>> getPolicies() {
        List<RetentionPolicyResponse> policies = retentionService.getAllPolicies();
        return ResponseEntity.ok(ResponseDto.ok(policies));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAuthority('RETENTION:POLICY:UPDATE') or hasRole('IT_ADMIN')")
    @Operation(summary = "Update data retention policy years and auto-archive flag")
    public ResponseEntity<ResponseDto<RetentionPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody RetentionPolicyUpdateRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        RetentionPolicyResponse updated = retentionService.updatePolicy(id, request, username);
        return ResponseEntity.ok(ResponseDto.ok(updated));
    }

    // ── Legal Hold Management ───────────────────────────────────────────────

    @GetMapping("/holds")
    @PreAuthorize("hasAuthority('RETENTION:HOLD:MANAGE') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Get list of legal hold records")
    public ResponseEntity<ResponseDto<Page<LegalHoldResponse>>> getLegalHolds(Pageable pageable) {
        Page<LegalHoldResponse> holds = retentionService.getLegalHolds(pageable);
        return ResponseEntity.ok(ResponseDto.ok(holds));
    }

    @PostMapping("/holds")
    @PreAuthorize("hasAuthority('RETENTION:HOLD:MANAGE') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Place a Legal Hold lock on an entity for FDA audit protection")
    public ResponseEntity<ResponseDto<LegalHoldResponse>> placeLegalHold(
            @Valid @RequestBody LegalHoldCreateRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        LegalHoldResponse placed = retentionService.placeLegalHold(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(placed));
    }

    @PostMapping("/holds/{id}/release")
    @PreAuthorize("hasAuthority('RETENTION:HOLD:MANAGE') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Release an active Legal Hold lock")
    public ResponseEntity<ResponseDto<LegalHoldResponse>> releaseLegalHold(
            @PathVariable Long id,
            @Valid @RequestBody LegalHoldReleaseRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        LegalHoldResponse released = retentionService.releaseLegalHold(id, request, username);
        return ResponseEntity.ok(ResponseDto.ok(released));
    }

    @GetMapping("/holds/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if an entity is currently under active legal hold")
    public ResponseEntity<ResponseDto<Map<String, Object>>> checkEntityHeld(
            @RequestParam String module,
            @RequestParam Long entityId) {
        boolean held = retentionService.isEntityHeld(module, entityId);
        return ResponseEntity.ok(ResponseDto.ok(Map.of("module", module, "entityId", entityId, "isHeld", held)));
    }
}
