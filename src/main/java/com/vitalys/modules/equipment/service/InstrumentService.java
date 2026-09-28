package com.vitalys.modules.equipment.service;

import com.vitalys.modules.equipment.dto.*;
import com.vitalys.modules.equipment.entity.CalibrationRecord;
import com.vitalys.modules.equipment.entity.Instrument;
import com.vitalys.modules.equipment.entity.MaintenanceRecord;
import com.vitalys.modules.equipment.repository.CalibrationRecordRepository;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.equipment.repository.MaintenanceRecordRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.entity.SysDepartment;
import com.vitalys.modules.sys.repository.SysDepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing analytical instruments, calibration events, and maintenance logs.
 * All modifying operations are audited for GxP and 21 CFR Part 11 compliance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final CalibrationRecordRepository calibrationRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final SysDepartmentRepository departmentRepository;

    /**
     * Search and list instruments with filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<InstrumentResponse> getInstruments(String search, String status, Long departmentId, Pageable pageable) {
        Page<Instrument> page = instrumentRepository.searchInstruments(search, status, departmentId, pageable);

        // Fetch department names in batch
        Map<Long, String> deptMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getName, (a, b) -> a));

        return page.map(inst -> toResponse(inst, deptMap.get(inst.getDepartmentId())));
    }

    /**
     * Get instrument details by ID including latest calibration status.
     */
    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentById(Long id) {
        Instrument inst = findOrThrow(id);
        String deptName = inst.getDepartmentId() != null
                ? departmentRepository.findById(inst.getDepartmentId()).map(SysDepartment::getName).orElse(null)
                : null;
        return toResponse(inst, deptName);
    }

    /**
     * Register a new laboratory instrument.
     */
    @Auditable(module = "EQUIPMENT", entity = "Instrument")
    @Transactional
    public InstrumentResponse createInstrument(InstrumentCreateRequest request) {
        if (instrumentRepository.existsByAssetCode(request.getAssetCode())) {
            throw new IllegalArgumentException("Asset code already exists: " + request.getAssetCode());
        }

        Instrument instrument = Instrument.builder()
                .departmentId(request.getDepartmentId())
                .name(request.getName())
                .model(request.getModel())
                .assetCode(request.getAssetCode())
                .serialNumber(request.getSerialNumber())
                .manufacturer(request.getManufacturer())
                .location(request.getLocation())
                .status(request.getStatus() != null ? request.getStatus() : "IN_SERVICE")
                .purchaseDate(request.getPurchaseDate())
                .notes(request.getNotes())
                .build();

        Instrument saved = instrumentRepository.save(instrument);
        log.info("Registered new instrument: {} [{}]", saved.getName(), saved.getAssetCode());

        String deptName = saved.getDepartmentId() != null
                ? departmentRepository.findById(saved.getDepartmentId()).map(SysDepartment::getName).orElse(null)
                : null;
        return toResponse(saved, deptName);
    }

    /**
     * Update an existing instrument's profile.
     */
    @Auditable(module = "EQUIPMENT", entity = "Instrument")
    @Transactional
    public InstrumentResponse updateInstrument(Long id, InstrumentUpdateRequest request) {
        Instrument instrument = findOrThrow(id);

        if (instrumentRepository.existsByAssetCodeAndIdNot(request.getAssetCode(), id)) {
            throw new IllegalArgumentException("Asset code is already in use by another instrument: " + request.getAssetCode());
        }

        instrument.setDepartmentId(request.getDepartmentId());
        instrument.setName(request.getName());
        instrument.setModel(request.getModel());
        instrument.setAssetCode(request.getAssetCode());
        instrument.setSerialNumber(request.getSerialNumber());
        instrument.setManufacturer(request.getManufacturer());
        instrument.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            instrument.setStatus(request.getStatus());
        }
        instrument.setPurchaseDate(request.getPurchaseDate());
        instrument.setNotes(request.getNotes());

        Instrument updated = instrumentRepository.save(instrument);
        log.info("Updated instrument ID {}: {} [{}]", updated.getId(), updated.getName(), updated.getAssetCode());

        String deptName = updated.getDepartmentId() != null
                ? departmentRepository.findById(updated.getDepartmentId()).map(SysDepartment::getName).orElse(null)
                : null;
        return toResponse(updated, deptName);
    }

    /**
     * Update instrument operational status.
     */
    @Auditable(module = "EQUIPMENT", entity = "Instrument")
    @Transactional
    public InstrumentResponse updateInstrumentStatus(Long id, String status) {
        Instrument instrument = findOrThrow(id);
        instrument.setStatus(status);
        Instrument updated = instrumentRepository.save(instrument);

        String deptName = updated.getDepartmentId() != null
                ? departmentRepository.findById(updated.getDepartmentId()).map(SysDepartment::getName).orElse(null)
                : null;
        return toResponse(updated, deptName);
    }

    /**
     * Delete / decommission an instrument.
     */
    @Auditable(module = "EQUIPMENT", entity = "Instrument")
    @Transactional
    public void deleteInstrument(Long id) {
        Instrument instrument = findOrThrow(id);
        instrumentRepository.delete(instrument);
        log.info("Deleted instrument ID {}: {}", id, instrument.getName());
    }

    // ── Calibration Management ────────────────────────────────────────────────

    /**
     * Log a new calibration event for an instrument.
     */
    @Auditable(module = "EQUIPMENT", entity = "CalibrationRecord")
    @Transactional
    public CalibrationRecordResponse addCalibrationRecord(CalibrationRecordRequest request) {
        Instrument instrument = findOrThrow(request.getInstrumentId());

        CalibrationRecord record = CalibrationRecord.builder()
                .instrumentId(instrument.getId())
                .performedAt(request.getPerformedAt() != null ? request.getPerformedAt() : OffsetDateTime.now())
                .nextDue(request.getNextDue())
                .performedBy(request.getPerformedBy())
                .certificateUrl(request.getCertificateUrl())
                .notes(request.getNotes())
                .build();

        CalibrationRecord saved = calibrationRepository.save(record);
        log.info("Logged calibration record for instrument ID {}: performed by {}", instrument.getId(), record.getPerformedBy());

        return toCalibrationResponse(saved, instrument);
    }

    /**
     * Get calibration history for an instrument.
     */
    @Transactional(readOnly = true)
    public List<CalibrationRecordResponse> getCalibrationHistory(Long instrumentId) {
        Instrument instrument = findOrThrow(instrumentId);
        return calibrationRepository.findByInstrumentIdOrderByPerformedAtDesc(instrumentId)
                .stream()
                .map(r -> toCalibrationResponse(r, instrument))
                .collect(Collectors.toList());
    }

    // ── Maintenance Management ────────────────────────────────────────────────

    /**
     * Log a maintenance service record.
     */
    @Auditable(module = "EQUIPMENT", entity = "MaintenanceRecord")
    @Transactional
    public MaintenanceRecordResponse addMaintenanceRecord(MaintenanceRecordRequest request) {
        Instrument instrument = findOrThrow(request.getInstrumentId());

        MaintenanceRecord record = MaintenanceRecord.builder()
                .instrumentId(instrument.getId())
                .performedAt(request.getPerformedAt() != null ? request.getPerformedAt() : OffsetDateTime.now())
                .nextDue(request.getNextDue())
                .performedBy(request.getPerformedBy())
                .description(request.getDescription())
                .build();

        MaintenanceRecord saved = maintenanceRepository.save(record);
        log.info("Logged maintenance record for instrument ID {}: {}", instrument.getId(), record.getDescription());

        return toMaintenanceResponse(saved, instrument);
    }

    /**
     * Get maintenance history for an instrument.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceHistory(Long instrumentId) {
        Instrument instrument = findOrThrow(instrumentId);
        return maintenanceRepository.findByInstrumentIdOrderByPerformedAtDesc(instrumentId)
                .stream()
                .map(r -> toMaintenanceResponse(r, instrument))
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Instrument findOrThrow(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Instrument not found with ID: " + id));
    }

    private InstrumentResponse toResponse(Instrument inst, String deptName) {
        // Query latest calibration record
        List<CalibrationRecord> calibrations = calibrationRepository.findByInstrumentIdOrderByPerformedAtDesc(inst.getId());
        OffsetDateTime lastCalDate = calibrations.isEmpty() ? null : calibrations.get(0).getPerformedAt();
        String nextCalDue = calibrations.isEmpty() ? null : calibrations.get(0).getNextDue();

        return InstrumentResponse.builder()
                .id(inst.getId())
                .departmentId(inst.getDepartmentId())
                .departmentName(deptName)
                .name(inst.getName())
                .model(inst.getModel())
                .assetCode(inst.getAssetCode())
                .serialNumber(inst.getSerialNumber())
                .manufacturer(inst.getManufacturer())
                .location(inst.getLocation())
                .status(inst.getStatus())
                .purchaseDate(inst.getPurchaseDate())
                .notes(inst.getNotes())
                .createdAt(inst.getCreatedAt())
                .updatedAt(inst.getUpdatedAt())
                .lastCalibrationDate(lastCalDate)
                .nextCalibrationDue(nextCalDue)
                .build();
    }

    private CalibrationRecordResponse toCalibrationResponse(CalibrationRecord record, Instrument inst) {
        return CalibrationRecordResponse.builder()
                .id(record.getId())
                .instrumentId(record.getInstrumentId())
                .instrumentName(inst != null ? inst.getName() : null)
                .instrumentAssetCode(inst != null ? inst.getAssetCode() : null)
                .performedAt(record.getPerformedAt())
                .nextDue(record.getNextDue())
                .performedBy(record.getPerformedBy())
                .certificateUrl(record.getCertificateUrl())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private MaintenanceRecordResponse toMaintenanceResponse(MaintenanceRecord record, Instrument inst) {
        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .instrumentId(record.getInstrumentId())
                .instrumentName(inst != null ? inst.getName() : null)
                .instrumentAssetCode(inst != null ? inst.getAssetCode() : null)
                .performedAt(record.getPerformedAt())
                .nextDue(record.getNextDue())
                .performedBy(record.getPerformedBy())
                .description(record.getDescription())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
