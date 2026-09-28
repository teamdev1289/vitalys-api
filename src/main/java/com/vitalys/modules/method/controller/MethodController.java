package com.vitalys.modules.method.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.service.MethodService;
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
 * Controller providing REST API for Standard Operating Procedures (SOP),
 * testing methods, and analytical validation protocols.
 */
@RestController
@RequestMapping("/api/v1/methods")
@RequiredArgsConstructor
@Tag(name = "Testing Methods & SOP", description = "Endpoints for managing testing methods, SOP steps, and validation protocols")
@SecurityRequirement(name = "bearerAuth")
public class MethodController {

    private final MethodService methodService;

    @Operation(summary = "List testing methods and SOPs with filtering")
    @GetMapping
    @PreAuthorize("hasAuthority('METHOD:SOP:READ')")
    public ResponseEntity<ResponseDto<List<MethodResponse>>> getMethods(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String validationStatus) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.getMethods(keyword, category, validationStatus)));
    }

    @Operation(summary = "Get method details by ID (including steps and validation protocols)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SOP:READ')")
    public ResponseEntity<ResponseDto<MethodDetailResponse>> getMethodById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.getMethodById(id)));
    }

    @Operation(summary = "Get method details by method code")
    @GetMapping("/code/{methodCode}")
    @PreAuthorize("hasAuthority('METHOD:SOP:READ')")
    public ResponseEntity<ResponseDto<MethodDetailResponse>> getMethodByCode(@PathVariable String methodCode) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.getMethodByCode(methodCode)));
    }

    @Operation(summary = "Create a new testing method / SOP")
    @PostMapping
    @PreAuthorize("hasAuthority('METHOD:SOP:CREATE')")
    public ResponseEntity<ResponseDto<MethodDetailResponse>> createMethod(@Valid @RequestBody MethodCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(methodService.createMethod(request)));
    }

    @Operation(summary = "Update an existing testing method")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SOP:UPDATE')")
    public ResponseEntity<ResponseDto<MethodResponse>> updateMethod(
            @PathVariable Long id,
            @Valid @RequestBody MethodUpdateRequest request) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.updateMethod(id, request)));
    }

    @Operation(summary = "Update method validation and lifecycle status (DRAFT, VALIDATED, RELEASED, DEPRECATED)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('METHOD:SOP:VALIDATE')")
    public ResponseEntity<ResponseDto<MethodResponse>> updateMethodStatus(
            @PathVariable Long id,
            @Valid @RequestBody MethodStatusUpdateRequest request) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.updateMethodStatus(id, request)));
    }

    @Operation(summary = "Delete or deprecate a testing method")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:SOP:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteMethod(@PathVariable Long id) {
        methodService.deleteMethod(id);
        return ResponseEntity.ok(ResponseDto.ok(null));
    }

    @Operation(summary = "Update execution steps of a testing method")
    @PutMapping("/{id}/steps")
    @PreAuthorize("hasAuthority('METHOD:SOP:UPDATE')")
    public ResponseEntity<ResponseDto<List<MethodStepResponse>>> updateSteps(
            @PathVariable Long id,
            @Valid @RequestBody List<MethodStepRequest> steps) {
        return ResponseEntity.ok(ResponseDto.ok(methodService.updateMethodSteps(id, steps)));
    }

    @Operation(summary = "Add an analytical method validation protocol (ICH Q2(R1))")
    @PostMapping("/{id}/validation-protocols")
    @PreAuthorize("hasAuthority('METHOD:SOP:VALIDATE')")
    public ResponseEntity<ResponseDto<MethodValidationProtocolResponse>> addValidationProtocol(
            @PathVariable Long id,
            @Valid @RequestBody MethodValidationProtocolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(methodService.addValidationProtocol(id, request)));
    }
}
