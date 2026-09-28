package com.vitalys.modules.sample.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sample.dto.*;
import com.vitalys.modules.sample.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Sample Accessioning, Tracking, and Lifecycle State Machine.
 */
@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Tag(name = "Sample Lifecycle Management", description = "Endpoints for sample accessioning, barcodes, status changes, and details")
@SecurityRequirement(name = "bearerAuth")
public class SampleController {

    private final SampleService sampleService;

    @Operation(summary = "List samples with search, status, department filters, and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:READ')")
    public ResponseEntity<ResponseDto<Page<SampleResponse>>> getSamples(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long requestId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(sampleService.getSamples(search, status, departmentId, requestId, pageable)));
    }

    @Operation(summary = "Get sample details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:READ')")
    public ResponseEntity<ResponseDto<SampleResponse>> getSampleById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(sampleService.getSampleById(id)));
    }

    @Operation(summary = "Lookup sample by barcode")
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:READ')")
    public ResponseEntity<ResponseDto<SampleResponse>> getSampleByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ResponseDto.ok(sampleService.getSampleByBarcode(barcode)));
    }

    @Operation(summary = "Get audit history of sample status transitions")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:READ')")
    public ResponseEntity<ResponseDto<List<SampleStatusHistoryResponse>>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(sampleService.getStatusHistory(id)));
    }

    @Operation(summary = "Accession physical sample containers and generate barcodes")
    @PostMapping("/accession")
    @PreAuthorize("hasAuthority('SAMPLE:ACCESSION:CREATE')")
    public ResponseEntity<ResponseDto<List<SampleResponse>>> accessionSamples(
            @Valid @RequestBody SampleAccessionRequest request) {
        List<SampleResponse> created = sampleService.accessionSamples(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Sample container(s) accessioned successfully", created));
    }

    @Operation(summary = "Update sample metadata, storage location, or testing assignment")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:UPDATE')")
    public ResponseEntity<ResponseDto<SampleResponse>> updateSample(
            @PathVariable Long id,
            @Valid @RequestBody SampleUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Sample updated successfully", sampleService.updateSample(id, request)));
    }

    @Operation(summary = "Execute state transition with mandatory GxP reason (21 CFR Part 11)")
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SAMPLE:SAMPLE:UPDATE')")
    public ResponseEntity<ResponseDto<SampleResponse>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody SampleStatusChangeRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Sample status transitioned successfully", sampleService.changeSampleStatus(id, request)));
    }
}
