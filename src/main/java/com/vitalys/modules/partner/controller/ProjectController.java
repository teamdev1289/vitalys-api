package com.vitalys.modules.partner.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.partner.dto.ProjectCreateRequest;
import com.vitalys.modules.partner.dto.ProjectResponse;
import com.vitalys.modules.partner.dto.ProjectUpdateRequest;
import com.vitalys.modules.partner.service.PartnerService;
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
 * Controller providing REST API for contract testing project management.
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "CRUD operations for analytical client projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final PartnerService partnerService;

    @Operation(summary = "List projects with search, customer filtering, and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:READ')")
    public ResponseEntity<ResponseDto<Page<ProjectResponse>>> getProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(partnerService.getProjects(search, customerId, status, pageable)));
    }

    @Operation(summary = "Get project details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:READ')")
    public ResponseEntity<ResponseDto<ProjectResponse>> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(partnerService.getProjectById(id)));
    }

    @Operation(summary = "Create a new project for customer")
    @PostMapping
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:CREATE')")
    public ResponseEntity<ResponseDto<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = partnerService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Update an existing project")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:UPDATE')")
    public ResponseEntity<ResponseDto<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Project updated successfully", partnerService.updateProject(id, request)));
    }

    @Operation(summary = "Delete project")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteProject(@PathVariable Long id) {
        partnerService.deleteProject(id);
        return ResponseEntity.ok(ResponseDto.noContent("Project deleted successfully"));
    }
}
