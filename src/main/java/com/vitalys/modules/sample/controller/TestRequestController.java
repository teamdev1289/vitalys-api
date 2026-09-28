package com.vitalys.modules.sample.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sample.dto.TestRequestCreateRequest;
import com.vitalys.modules.sample.dto.TestRequestResponse;
import com.vitalys.modules.sample.dto.TestRequestUpdateRequest;
import com.vitalys.modules.sample.service.TestRequestService;
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

import java.util.Map;

/**
 * Controller for Managing Laboratory Test Requests (Yêu cầu kiểm nghiệm).
 */
@RestController
@RequestMapping("/api/v1/test-requests")
@RequiredArgsConstructor
@Tag(name = "Test Request Management", description = "Endpoints for submitting and managing testing requests")
@SecurityRequirement(name = "bearerAuth")
public class TestRequestController {

    private final TestRequestService testRequestService;

    @Operation(summary = "List test requests with filters and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('SAMPLE:REQUEST:READ')")
    public ResponseEntity<ResponseDto<Page<TestRequestResponse>>> getRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String sourceType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(testRequestService.getRequests(search, status, priority, sourceType, pageable)));
    }

    @Operation(summary = "Get test request details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SAMPLE:REQUEST:READ')")
    public ResponseEntity<ResponseDto<TestRequestResponse>> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(testRequestService.getRequestById(id)));
    }

    @Operation(summary = "Submit a new testing request")
    @PostMapping
    @PreAuthorize("hasAuthority('SAMPLE:REQUEST:CREATE')")
    public ResponseEntity<ResponseDto<TestRequestResponse>> createRequest(
            @Valid @RequestBody TestRequestCreateRequest request) {
        TestRequestResponse created = testRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Test request submitted successfully", created));
    }

    @Operation(summary = "Update an existing test request")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SAMPLE:REQUEST:UPDATE')")
    public ResponseEntity<ResponseDto<TestRequestResponse>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody TestRequestUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Test request updated successfully", testRequestService.updateRequest(id, request)));
    }

    @Operation(summary = "Cancel an unreceived test request")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SAMPLE:REQUEST:CANCEL')")
    public ResponseEntity<ResponseDto<TestRequestResponse>> cancelRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Cancelled by user";
        return ResponseEntity.ok(
                ResponseDto.ok("Test request cancelled", testRequestService.cancelRequest(id, reason)));
    }
}
