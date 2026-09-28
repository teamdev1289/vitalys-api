package com.vitalys.modules.executive.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.executive.dto.ExecutiveDashboardMetrics;
import com.vitalys.modules.executive.service.ExecutiveDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard/executive")
@RequiredArgsConstructor
@Tag(name = "Executive Dashboard", description = "Aggregated Compliance, Operational & Throughput Metrics")
public class ExecutiveDashboardController {

    private final ExecutiveDashboardService executiveDashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD:EXECUTIVE:READ') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get aggregated executive lab dashboard metrics and charts")
    public ResponseEntity<ResponseDto<ExecutiveDashboardMetrics>> getExecutiveMetrics() {
        ExecutiveDashboardMetrics metrics = executiveDashboardService.getMetrics();
        return ResponseEntity.ok(ResponseDto.ok(metrics));
    }
}
