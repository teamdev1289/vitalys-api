package com.vitalys.modules.sys.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sys.dto.department.DepartmentRequest;
import com.vitalys.modules.sys.dto.department.DepartmentResponse;
import com.vitalys.modules.sys.service.DepartmentService;
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

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "Laboratory department management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Get all laboratory departments")
    @GetMapping
    public ResponseEntity<ResponseDto<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ResponseDto.ok(departmentService.getAllDepartments()));
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(departmentService.getDepartmentById(id)));
    }

    @Operation(summary = "Create a new department")
    @PostMapping
    @PreAuthorize("hasAuthority('SYS:DEPARTMENT:CREATE')")
    public ResponseEntity<ResponseDto<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created(departmentService.createDepartment(request)));
    }

    @Operation(summary = "Update an existing department")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS:DEPARTMENT:UPDATE')")
    public ResponseEntity<ResponseDto<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ResponseDto.ok("Department updated successfully", departmentService.updateDepartment(id, request)));
    }

    @Operation(summary = "Delete department")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS:DEPARTMENT:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ResponseDto.ok("Department deleted successfully", null));
    }
}
