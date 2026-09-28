package com.vitalys.modules.testing.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.testing.dto.OosInvestigationRequest;
import com.vitalys.modules.testing.dto.OosInvestigationResponse;
import com.vitalys.modules.testing.service.OosInvestigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/oos")
@RequiredArgsConstructor
@Tag(name = "OOS Investigations", description = "Endpoints for Out of Specification (OOS) Phase 1 Lab Investigations")
@SecurityRequirement(name = "bearerAuth")
public class OosInvestigationController {

    private final OosInvestigationService oosService;

    @Operation(summary = "List OOS investigations with search, phase, and status filters")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('TESTING:OOS:INVESTIGATE', 'TESTING:WORKLIST:READ', 'TESTING:RESULT:ENTER', 'TESTING:TEST:READ')")
    public ResponseEntity<ResponseDto<Page<OosInvestigationResponse>>> getInvestigations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(oosService.getInvestigations(search, phase, status, pageable)));
    }

    @Operation(summary = "Get OOS investigation details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TESTING:OOS:INVESTIGATE', 'TESTING:WORKLIST:READ', 'TESTING:RESULT:ENTER', 'TESTING:TEST:READ')")
    public ResponseEntity<ResponseDto<OosInvestigationResponse>> getInvestigationById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(oosService.getInvestigationById(id)));
    }

    @Operation(summary = "Update OOS investigation findings, supervisor review, and conclusion")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TESTING:OOS:INVESTIGATE')")
    public ResponseEntity<ResponseDto<OosInvestigationResponse>> updateInvestigation(
            @PathVariable Long id,
            @Valid @RequestBody OosInvestigationRequest request) {
        return ResponseEntity.ok(ResponseDto.ok("OOS Investigation updated", oosService.updateInvestigation(id, request)));
    }
}
