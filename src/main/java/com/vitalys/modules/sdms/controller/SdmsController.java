package com.vitalys.modules.sdms.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sdms.dto.ChromatogramDataResponse;
import com.vitalys.modules.sdms.dto.DataFileLinkRequest;
import com.vitalys.modules.sdms.dto.DataFileResponse;
import com.vitalys.modules.sdms.service.SdmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/sdms")
@RequiredArgsConstructor
@Tag(name = "SDMS (Scientific Data Management System)", description = "Scientific instrument raw data ingestion, indexing, and InSpector chromatogram viewing")
@SecurityRequirement(name = "bearerAuth")
public class SdmsController {

    private final SdmsService sdmsService;

    @Operation(summary = "Upload raw scientific data file from instrument (HPLC, GC, UV-Vis, FTIR)")
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SDMS:DATA:UPLOAD')")
    public ResponseEntity<ResponseDto<DataFileResponse>> uploadDataFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) Long sampleId,
            @RequestParam(required = false) Long testId,
            @RequestParam(required = false) Long runId,
            @RequestParam(required = false) String notes
    ) {
        DataFileResponse response = sdmsService.uploadFile(file, fileType, instrumentId, sampleId, testId, runId, notes);
        return ResponseEntity.ok(ResponseDto.ok("Tải lên và lập chỉ mục dữ liệu khoa học thành công (SHA-256 Verified)", response));
    }

    @Operation(summary = "Search and list ingested scientific data files")
    @GetMapping("/files")
    @PreAuthorize("hasAuthority('SDMS:DATA:READ')")
    public ResponseEntity<ResponseDto<Page<DataFileResponse>>> getFiles(
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) Long sampleId,
            @RequestParam(required = false) Long testId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<DataFileResponse> page = sdmsService.getFiles(fileType, instrumentId, sampleId, testId, search, pageable);
        return ResponseEntity.ok(ResponseDto.ok(page));
    }

    @Operation(summary = "Get scientific data file details, metadata, and detected peaks by ID")
    @GetMapping("/files/{id}")
    @PreAuthorize("hasAuthority('SDMS:DATA:READ')")
    public ResponseEntity<ResponseDto<DataFileResponse>> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(sdmsService.getFileById(id)));
    }

    @Operation(summary = "Get time-series coordinate points and peak annotations for InSpector 2D interactive viewer")
    @GetMapping("/files/{id}/chromatogram")
    @PreAuthorize("hasAuthority('SDMS:DATA:READ')")
    public ResponseEntity<ResponseDto<ChromatogramDataResponse>> getChromatogramData(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(sdmsService.getChromatogramData(id)));
    }

    @Operation(summary = "Attach or link a scientific data file to a sample, test, or analytical run")
    @PostMapping("/files/{id}/link")
    @PreAuthorize("hasAuthority('SDMS:LINK:ATTACH')")
    public ResponseEntity<ResponseDto<DataFileResponse>> linkFile(
            @PathVariable Long id,
            @RequestBody DataFileLinkRequest request
    ) {
        DataFileResponse response = sdmsService.linkFile(id, request);
        return ResponseEntity.ok(ResponseDto.ok("Liên kết dữ liệu phân tích thành công", response));
    }

    @Operation(summary = "Download raw analytical data file with SHA-256 checksum verification header")
    @GetMapping("/files/{id}/download")
    @PreAuthorize("hasAuthority('SDMS:DATA:READ')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        DataFileResponse fileMeta = sdmsService.getFileById(id);
        byte[] bytes = sdmsService.downloadRawFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMeta.getOriginalFilename() + "\"")
                .header("X-Vitalys-Checksum-SHA256", fileMeta.getChecksumSha256())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
