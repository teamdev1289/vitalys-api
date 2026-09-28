package com.vitalys.modules.method.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.service.FormTemplateService;
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
 * Controller providing REST API for dynamic test experiment form templates.
 */
@RestController
@RequestMapping("/api/v1/form-templates")
@RequiredArgsConstructor
@Tag(name = "Dynamic Form Templates", description = "Endpoints for managing dynamic experiment forms and calculation fields")
@SecurityRequirement(name = "bearerAuth")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @Operation(summary = "List dynamic form templates with optional methodId filter")
    @GetMapping
    @PreAuthorize("hasAuthority('METHOD:FORM:READ')")
    public ResponseEntity<ResponseDto<List<FormTemplateResponse>>> getTemplates(
            @RequestParam(required = false) Long methodId) {
        return ResponseEntity.ok(ResponseDto.ok(formTemplateService.getTemplates(methodId)));
    }

    @Operation(summary = "Get form template details and all configured fields by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:FORM:READ')")
    public ResponseEntity<ResponseDto<FormTemplateResponse>> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(formTemplateService.getTemplateById(id)));
    }

    @Operation(summary = "Get active form template for a testing method (used by analyst testing screen)")
    @GetMapping("/by-method/{methodId}")
    @PreAuthorize("hasAuthority('METHOD:FORM:READ')")
    public ResponseEntity<ResponseDto<FormTemplateResponse>> getActiveTemplateByMethodId(@PathVariable Long methodId) {
        return ResponseEntity.ok(ResponseDto.ok(formTemplateService.getActiveTemplateByMethodId(methodId)));
    }

    @Operation(summary = "Create a new dynamic form template with fields")
    @PostMapping
    @PreAuthorize("hasAuthority('METHOD:FORM:CREATE')")
    public ResponseEntity<ResponseDto<FormTemplateResponse>> createTemplate(
            @Valid @RequestBody FormTemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(formTemplateService.createTemplate(request)));
    }

    @Operation(summary = "Update form template header")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:FORM:UPDATE')")
    public ResponseEntity<ResponseDto<FormTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody FormTemplateUpdateRequest request) {
        return ResponseEntity.ok(ResponseDto.ok(formTemplateService.updateTemplate(id, request)));
    }

    @Operation(summary = "Update fields of a form template")
    @PutMapping("/{id}/fields")
    @PreAuthorize("hasAuthority('METHOD:FORM:UPDATE')")
    public ResponseEntity<ResponseDto<List<FormFieldResponse>>> updateTemplateFields(
            @PathVariable Long id,
            @Valid @RequestBody List<FormFieldRequest> fields) {
        return ResponseEntity.ok(ResponseDto.ok(formTemplateService.updateTemplateFields(id, fields)));
    }

    @Operation(summary = "Delete dynamic form template")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('METHOD:FORM:UPDATE')")
    public ResponseEntity<ResponseDto<Void>> deleteTemplate(@PathVariable Long id) {
        formTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ResponseDto.ok(null));
    }
}
