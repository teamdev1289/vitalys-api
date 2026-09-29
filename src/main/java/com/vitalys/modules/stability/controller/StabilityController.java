package com.vitalys.modules.stability.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.stability.dto.*;
import com.vitalys.modules.stability.service.StabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/stability")
@RequiredArgsConstructor
public class StabilityController {

    private final StabilityService stabilityService;

    // ── Studies Management ─────────────────────────────────────────────────────

    @GetMapping("/studies")
    @PreAuthorize("hasAuthority('STABILITY:STUDY:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<Page<StabilityStudyResponse>>> searchStudies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(stabilityService.searchStudies(status, productId, search, pageable)));
    }

    @GetMapping("/studies/{id}")
    @PreAuthorize("hasAuthority('STABILITY:STUDY:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<StabilityStudyResponse>> getStudyById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(stabilityService.getStudyById(id)));
    }

    @GetMapping("/studies/{id}/pull-events")
    @PreAuthorize("hasAuthority('STABILITY:PULL:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<java.util.List<StabilityPullEventResponse>>> getPullEventsByStudyId(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(stabilityService.getPullEventsByStudyId(id)));
    }

    @PostMapping("/studies")
    @PreAuthorize("hasAuthority('STABILITY:STUDY:CREATE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<StabilityStudyResponse>> createStudy(
            @Valid @RequestBody StabilityStudyCreateRequest req,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "supervisor";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Khởi tạo nghiên cứu độ ổn định thành công", stabilityService.createStudy(req, username)));
    }

    // ── Sample Pull Events ─────────────────────────────────────────────────────

    @GetMapping("/pull-events")
    @PreAuthorize("hasAuthority('STABILITY:PULL:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<Page<StabilityPullEventResponse>>> searchPullEvents(
            @RequestParam(required = false) Long studyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(stabilityService.searchPullEvents(studyId, status, fromDate, toDate, pageable)));
    }

    @PostMapping("/pull-events/{id}/execute")
    @PreAuthorize("hasAuthority('STABILITY:PULL:EXECUTE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<StabilityPullEventResponse>> executePull(
            @PathVariable Long id,
            @Valid @RequestBody StabilityPullActionRequest req,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "operator";
        return ResponseEntity.ok(ResponseDto.ok("Thực hiện rút mẫu ổn định thành công", stabilityService.executePull(id, req, username)));
    }

    // ── Statistical Trend Analysis & Shelf-life Estimation ────────────────────

    @GetMapping("/studies/{id}/trend")
    @PreAuthorize("hasAuthority('STABILITY:PULL:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<StabilityTrendResponse>> getStabilityTrend(
            @PathVariable Long id,
            @RequestParam(required = false) Long conditionId) {
        return ResponseEntity.ok(ResponseDto.ok(stabilityService.calculateTrend(id, conditionId)));
    }
}
