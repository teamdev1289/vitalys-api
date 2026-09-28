package com.vitalys.modules.testing.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.testing.dto.AnalyticalRunRequest;
import com.vitalys.modules.testing.dto.AnalyticalRunResponse;
import com.vitalys.modules.testing.service.AnalyticalRunService;
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

@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
@Tag(name = "Analytical Runs", description = "Endpoints for laboratory instrument runs and analytical sequences")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticalRunController {

    private final AnalyticalRunService runService;

    @Operation(summary = "List analytical runs with filters and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('TESTING:RUN:READ')")
    public ResponseEntity<ResponseDto<Page<AnalyticalRunResponse>>> getRuns(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(runService.getRuns(search, status, pageable)));
    }

    @Operation(summary = "Get analytical run details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TESTING:RUN:READ')")
    public ResponseEntity<ResponseDto<AnalyticalRunResponse>> getRunById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(runService.getRunById(id)));
    }

    @Operation(summary = "Create a new analytical run sequence")
    @PostMapping
    @PreAuthorize("hasAuthority('TESTING:RUN:CREATE')")
    public ResponseEntity<ResponseDto<AnalyticalRunResponse>> createRun(@Valid @RequestBody AnalyticalRunRequest request) {
        AnalyticalRunResponse created = runService.createRun(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Analytical run sequence created", created));
    }
}
