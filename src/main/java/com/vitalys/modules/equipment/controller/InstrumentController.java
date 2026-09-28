package com.vitalys.modules.equipment.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.equipment.dto.*;
import com.vitalys.modules.equipment.service.InstrumentService;
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
 * Controller providing REST API for laboratory instruments and calibration/maintenance logs.
 * Protected by Spring Security and permissions in EQUIPMENT module.
 */
@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
@Tag(name = "Equipment Management", description = "CRUD and lifecycle operations for lab instruments")
@SecurityRequirement(name = "bearerAuth")
public class InstrumentController {

    private final InstrumentService instrumentService;

    @Operation(summary = "List instruments with filtering, search and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:READ')")
    public ResponseEntity<ResponseDto<Page<InstrumentResponse>>> getInstruments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(instrumentService.getInstruments(search, status, departmentId, pageable)));
    }

    @Operation(summary = "Get instrument by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:READ')")
    public ResponseEntity<ResponseDto<InstrumentResponse>> getInstrument(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(instrumentService.getInstrumentById(id)));
    }

    @Operation(summary = "Register a new laboratory instrument")
    @PostMapping
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:CREATE')")
    public ResponseEntity<ResponseDto<InstrumentResponse>> createInstrument(
            @Valid @RequestBody InstrumentCreateRequest request) {
        InstrumentResponse response = instrumentService.createInstrument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Update an existing instrument")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:UPDATE')")
    public ResponseEntity<ResponseDto<InstrumentResponse>> updateInstrument(
            @PathVariable Long id,
            @Valid @RequestBody InstrumentUpdateRequest request) {
        return ResponseEntity.ok(ResponseDto.ok("Instrument updated successfully", instrumentService.updateInstrument(id, request)));
    }

    @Operation(summary = "Update instrument status (IN_SERVICE, MAINTENANCE, OUT_OF_SERVICE)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:UPDATE')")
    public ResponseEntity<ResponseDto<InstrumentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ResponseDto.ok("Status updated", instrumentService.updateInstrumentStatus(id, status)));
    }

    @Operation(summary = "Delete / Decommission instrument")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteInstrument(@PathVariable Long id) {
        instrumentService.deleteInstrument(id);
        return ResponseEntity.ok(ResponseDto.noContent("Instrument deleted successfully"));
    }

    @Operation(summary = "Log a calibration record for instrument")
    @PostMapping("/{id}/calibrations")
    @PreAuthorize("hasAuthority('EQUIPMENT:CALIBRATION:CREATE')")
    public ResponseEntity<ResponseDto<CalibrationRecordResponse>> addCalibration(
            @PathVariable Long id,
            @Valid @RequestBody CalibrationRecordRequest request) {
        request.setInstrumentId(id);
        CalibrationRecordResponse response = instrumentService.addCalibrationRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Get calibration history for instrument")
    @GetMapping("/{id}/calibrations")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:READ')")
    public ResponseEntity<ResponseDto<List<CalibrationRecordResponse>>> getCalibrationHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(instrumentService.getCalibrationHistory(id)));
    }

    @Operation(summary = "Log a maintenance record for instrument")
    @PostMapping("/{id}/maintenances")
    @PreAuthorize("hasAuthority('EQUIPMENT:MAINTENANCE:CREATE')")
    public ResponseEntity<ResponseDto<MaintenanceRecordResponse>> addMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRecordRequest request) {
        request.setInstrumentId(id);
        MaintenanceRecordResponse response = instrumentService.addMaintenanceRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Get maintenance history for instrument")
    @GetMapping("/{id}/maintenances")
    @PreAuthorize("hasAuthority('EQUIPMENT:INSTRUMENT:READ')")
    public ResponseEntity<ResponseDto<List<MaintenanceRecordResponse>>> getMaintenanceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(instrumentService.getMaintenanceHistory(id)));
    }
}
