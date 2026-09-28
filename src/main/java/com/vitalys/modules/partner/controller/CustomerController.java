package com.vitalys.modules.partner.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.partner.dto.CustomerCreateRequest;
import com.vitalys.modules.partner.dto.CustomerResponse;
import com.vitalys.modules.partner.dto.CustomerUpdateRequest;
import com.vitalys.modules.partner.dto.ProjectResponse;
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

import java.util.List;

/**
 * Controller providing REST API for external customer/partner management.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "CRUD operations for external clients and sponsors")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final PartnerService partnerService;

    @Operation(summary = "List customers with search, status filtering, and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('PARTNER:CUSTOMER:READ')")
    public ResponseEntity<ResponseDto<Page<CustomerResponse>>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(partnerService.getCustomers(search, status, pageable)));
    }

    @Operation(summary = "Get customer details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:CUSTOMER:READ')")
    public ResponseEntity<ResponseDto<CustomerResponse>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(partnerService.getCustomerById(id)));
    }

    @Operation(summary = "Get all projects for a customer")
    @GetMapping("/{id}/projects")
    @PreAuthorize("hasAuthority('PARTNER:PROJECT:READ')")
    public ResponseEntity<ResponseDto<List<ProjectResponse>>> getCustomerProjects(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(partnerService.getProjectsByCustomer(id)));
    }

    @Operation(summary = "Register a new customer")
    @PostMapping
    @PreAuthorize("hasAuthority('PARTNER:CUSTOMER:CREATE')")
    public ResponseEntity<ResponseDto<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = partnerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Update customer profile")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:CUSTOMER:UPDATE')")
    public ResponseEntity<ResponseDto<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Customer updated successfully", partnerService.updateCustomer(id, request)));
    }

    @Operation(summary = "Delete customer (only if no active projects)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTNER:CUSTOMER:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteCustomer(@PathVariable Long id) {
        partnerService.deleteCustomer(id);
        return ResponseEntity.ok(ResponseDto.noContent("Customer deleted successfully"));
    }
}
