package com.vitalys.modules.product.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.product.dto.BatchCreateRequest;
import com.vitalys.modules.product.dto.BatchResponse;
import com.vitalys.modules.product.dto.BatchUpdateRequest;
import com.vitalys.modules.product.service.ProductService;
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

/**
 * Controller providing REST API for manufacturing batch tracking and release.
 */
@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Tag(name = "Batch Management", description = "CRUD and release operations for production batches")
@SecurityRequirement(name = "bearerAuth")
public class BatchController {

    private final ProductService productService;

    @Operation(summary = "List batches with search, product filtering, status, and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:READ')")
    public ResponseEntity<ResponseDto<Page<BatchResponse>>> getBatches(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(productService.getBatches(search, productId, status, pageable)));
    }

    @Operation(summary = "Get batch by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:READ')")
    public ResponseEntity<ResponseDto<BatchResponse>> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(productService.getBatchById(id)));
    }

    @Operation(summary = "Register a new manufacturing batch")
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:CREATE')")
    public ResponseEntity<ResponseDto<BatchResponse>> createBatch(
            @Valid @RequestBody BatchCreateRequest request) {
        BatchResponse response = productService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Update an existing batch")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:UPDATE')")
    public ResponseEntity<ResponseDto<BatchResponse>> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody BatchUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Batch updated successfully", productService.updateBatch(id, request)));
    }

    @Operation(summary = "Update batch QA release status (QUARANTINE, RELEASED, REJECTED, IN_TESTING)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:UPDATE')")
    public ResponseEntity<ResponseDto<BatchResponse>> updateBatchStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                ResponseDto.ok("Status updated", productService.updateBatchStatus(id, status)));
    }

    @Operation(summary = "Delete batch")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteBatch(@PathVariable Long id) {
        productService.deleteBatch(id);
        return ResponseEntity.ok(ResponseDto.noContent("Batch deleted successfully"));
    }
}
