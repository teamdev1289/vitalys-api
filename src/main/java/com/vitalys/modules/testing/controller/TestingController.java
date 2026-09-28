package com.vitalys.modules.testing.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.testing.dto.*;
import com.vitalys.modules.testing.service.TestingService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
@Tag(name = "Testing & Worklist", description = "Endpoints for analyst worklist, test execution, results, and revisions")
@SecurityRequirement(name = "bearerAuth")
public class TestingController {

    private final TestingService testingService;

    @Operation(summary = "Get analyst worklist and test queue")
    @GetMapping
    @PreAuthorize("hasAuthority('TESTING:WORKLIST:READ')")
    public ResponseEntity<ResponseDto<Page<TestResponse>>> getWorklist(
            @RequestParam(required = false) String analyst,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(testingService.getWorklist(analyst, status, priority, search, pageable)));
    }

    @Operation(summary = "Get personal worklist for currently authenticated analyst")
    @GetMapping("/my-worklist")
    @PreAuthorize("hasAuthority('TESTING:WORKLIST:READ')")
    public ResponseEntity<ResponseDto<Page<TestResponse>>> getMyWorklist(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;
        return ResponseEntity.ok(ResponseDto.ok(testingService.getWorklist(currentUsername, status, priority, search, pageable)));
    }

    @Operation(summary = "Get test details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TESTING:TEST:READ')")
    public ResponseEntity<ResponseDto<TestResponse>> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(testingService.getTestById(id)));
    }

    @Operation(summary = "Get tests for a specific sample")
    @GetMapping("/sample/{sampleId}")
    @PreAuthorize("hasAuthority('TESTING:TEST:READ')")
    public ResponseEntity<ResponseDto<List<TestResponse>>> getTestsBySampleId(@PathVariable Long sampleId) {
        return ResponseEntity.ok(ResponseDto.ok(testingService.getTestsBySampleId(sampleId)));
    }

    @Operation(summary = "Assign a new test procedure to an analyst")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('TESTING:TEST:ASSIGN')")
    public ResponseEntity<ResponseDto<TestResponse>> assignTest(@Valid @RequestBody TestAssignmentRequest request) {
        TestResponse assigned = testingService.assignTest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Test procedure assigned successfully", assigned));
    }

    @Operation(summary = "Re-assign existing test procedure to an analyst")
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('TESTING:TEST:ASSIGN')")
    public ResponseEntity<ResponseDto<TestResponse>> reassignTest(
            @PathVariable Long id,
            @RequestBody TestAssignmentRequest request) {
        TestResponse assigned = testingService.reassignTest(id, request);
        return ResponseEntity.ok(ResponseDto.ok("Test procedure assigned successfully", assigned));
    }

    @Operation(summary = "Start executing a test procedure")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('TESTING:TEST:EXECUTE')")
    public ResponseEntity<ResponseDto<TestResponse>> startTest(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok("Test procedure started", testingService.startTest(id)));
    }

    @Operation(summary = "Complete test procedure")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('TESTING:TEST:EXECUTE')")
    public ResponseEntity<ResponseDto<TestResponse>> completeTest(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok("Test procedure completed", testingService.completeTest(id)));
    }

    @Operation(summary = "Enter test result with automatic Specification and OOS evaluation")
    @PostMapping("/results")
    @PreAuthorize("hasAuthority('TESTING:RESULT:ENTER')")
    public ResponseEntity<ResponseDto<ResultResponse>> enterResult(@Valid @RequestBody ResultEntryRequest request) {
        ResultResponse response = testingService.enterResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Test result recorded successfully", response));
    }

    @Operation(summary = "Enter test result for a specific test")
    @PostMapping("/{testId}/results")
    @PreAuthorize("hasAuthority('TESTING:RESULT:ENTER')")
    public ResponseEntity<ResponseDto<ResultResponse>> enterResultForTest(
            @PathVariable Long testId,
            @RequestBody ResultEntryRequest request) {
        request.setTestId(testId);
        ResultResponse response = testingService.enterResult(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Test result recorded successfully", response));
    }

    @Operation(summary = "Revise test result with mandatory GxP justification reason (ALCOA+)")
    @PostMapping("/results/{resultId}/revise")
    @PreAuthorize("hasAuthority('TESTING:RESULT:REVISE')")
    public ResponseEntity<ResponseDto<ResultResponse>> reviseResult(
            @PathVariable Long resultId,
            @Valid @RequestBody ResultRevisionRequest request) {
        return ResponseEntity.ok(ResponseDto.ok("Result revised successfully", testingService.reviseResult(resultId, request)));
    }

    @Operation(summary = "Revise test result with mandatory GxP justification reason (ALCOA+)")
    @PutMapping("/results/{resultId}")
    @PreAuthorize("hasAuthority('TESTING:RESULT:REVISE')")
    public ResponseEntity<ResponseDto<ResultResponse>> reviseResultPut(
            @PathVariable Long resultId,
            @Valid @RequestBody ResultRevisionRequest request) {
        return ResponseEntity.ok(ResponseDto.ok("Result revised successfully", testingService.reviseResult(resultId, request)));
    }
}
