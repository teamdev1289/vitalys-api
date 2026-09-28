package com.vitalys.modules.method.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.service.SpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing REST API for pharmaceutical quality specifications
 * and release criteria.
 */
@RestController
@RequestMapping("/api/v1/specifications")
@RequiredArgsConstructor
@Tag(name = "Quality Specifications", description = "Endpoints for managing release criteria, specification sets and items")
@SecurityRequirement(name = "bearerAuth")
public class SpecificationController {

    private final SpecificationService specificationService;

    @Operation(summary = "List specification sets with product, market, and status filters")
    @GetMapping
    @PreAuthorize("hasAuthority('METHOD:SPEC:READ')")
    public ResponseEntity<ResponseDto<List<SpecificationSetResponse>>> getSpecificationSets(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ResponseDto.ok(specificationService.getSpecificationSets(productId, market, status)));
    }

    @Operation(summary = "Get specification set details with criteria items by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SPEC:READ')")
    public ResponseEntity<ResponseDto<SpecificationSetDetailResponse>> getSpecificationSetById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(specificationService.getSpecificationSetById(id)));
    }

    @Operation(summary = "Get active specification set for a product and market (used during batch test release)")
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('METHOD:SPEC:READ')")
    public ResponseEntity<ResponseDto<SpecificationSetDetailResponse>> getActiveSpecification(
            @RequestParam Long productId,
            @RequestParam(required = false) String market) {
        return ResponseEntity.ok(ResponseDto.ok(specificationService.getActiveSpecification(productId, market)));
    }

    @Operation(summary = "Create a new specification set with criteria items")
    @PostMapping
    @PreAuthorize("hasAuthority('METHOD:SPEC:CREATE')")
    public ResponseEntity<ResponseDto<SpecificationSetDetailResponse>> createSpecificationSet(
            @Valid @RequestBody SpecificationSetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(specificationService.createSpecificationSet(request)));
    }

    @Operation(summary = "Update specification set header metadata")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SPEC:UPDATE')")
    public ResponseEntity<ResponseDto<SpecificationSetResponse>> updateSpecificationSet(
            @PathVariable Long id,
            @Valid @RequestBody SpecificationSetUpdateRequest request) {
        return ResponseEntity.ok(ResponseDto.ok(specificationService.updateSpecificationSet(id, request)));
    }

    @Operation(summary = "Update specification items for a set")
    @PutMapping("/{id}/items")
    @PreAuthorize("hasAuthority('METHOD:SPEC:UPDATE')")
    public ResponseEntity<ResponseDto<List<SpecificationItemResponse>>> updateSpecificationItems(
            @PathVariable Long id,
            @Valid @RequestBody List<SpecificationItemRequest> items) {
        return ResponseEntity.ok(ResponseDto.ok(specificationService.updateSpecificationItems(id, items)));
    }

    @Operation(summary = "Delete or archive a specification set")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SPEC:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteSpecificationSet(@PathVariable Long id) {
        specificationService.deleteSpecificationSet(id);
        return ResponseEntity.ok(ResponseDto.ok(null));
    }
}
