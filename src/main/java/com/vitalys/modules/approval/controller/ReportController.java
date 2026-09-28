package com.vitalys.modules.approval.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.approval.dto.ReportGenerateRequest;
import com.vitalys.modules.approval.dto.ReportResponse;
import com.vitalys.modules.approval.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Certificate of Analysis (COA)", description = "Official GxP report management and ISO/IEC 17025 COA PDF generation")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "List all reports (COA, Analytical Summaries)")
    @GetMapping
    @PreAuthorize("hasAuthority('APPROVAL:REPORT:READ')")
    public ResponseEntity<ResponseDto<Page<ReportResponse>>> getAllReports(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseDto.ok(reportService.getAllReports(pageable)));
    }

    @Operation(summary = "Get report details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPROVAL:REPORT:READ')")
    public ResponseEntity<ResponseDto<ReportResponse>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(reportService.getReportById(id)));
    }

    @Operation(summary = "Get reports associated with a specific sample")
    @GetMapping("/sample/{sampleId}")
    @PreAuthorize("hasAuthority('APPROVAL:REPORT:READ')")
    public ResponseEntity<ResponseDto<List<ReportResponse>>> getReportsBySample(@PathVariable Long sampleId) {
        return ResponseEntity.ok(ResponseDto.ok(reportService.getReportsBySampleId(sampleId)));
    }

    @Operation(summary = "Generate or compile a Certificate of Analysis (COA) for a sample")
    @PostMapping("/coa/generate")
    @PreAuthorize("hasAuthority('APPROVAL:REPORT:GENERATE')")
    public ResponseEntity<ResponseDto<ReportResponse>> generateCoa(
            @Valid @RequestBody ReportGenerateRequest request
    ) {
        ReportResponse response = reportService.generateCoa(request);
        return ResponseEntity.ok(ResponseDto.ok("Khởi tạo Phiếu Kiểm Nghiệm (COA) thành công", response));
    }

    @Operation(summary = "Download or stream the official Certificate of Analysis (COA) PDF document")
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('APPROVAL:REPORT:READ')")
    public ResponseEntity<byte[]> downloadCoaPdf(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        byte[] pdfBytes = reportService.getPdfBytes(id);

        String filename = (report.getReportCode() != null ? report.getReportCode() : "COA-" + id) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
