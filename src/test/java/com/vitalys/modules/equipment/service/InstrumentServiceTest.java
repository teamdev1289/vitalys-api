package com.vitalys.modules.equipment.service;

import com.vitalys.modules.equipment.dto.*;
import com.vitalys.modules.equipment.entity.CalibrationRecord;
import com.vitalys.modules.equipment.entity.Instrument;
import com.vitalys.modules.equipment.repository.CalibrationRecordRepository;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.equipment.repository.MaintenanceRecordRepository;
import com.vitalys.modules.sys.repository.SysDepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private CalibrationRecordRepository calibrationRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRepository;

    @Mock
    private SysDepartmentRepository departmentRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    private Instrument sampleInstrument;

    @BeforeEach
    void setUp() {
        sampleInstrument = Instrument.builder()
                .id(1L)
                .name("Waters ACQUITY UPLC")
                .model("H-Class")
                .assetCode("EQ-HPLC-001")
                .serialNumber("SN-12345")
                .manufacturer("Waters Corporation")
                .location("Room 201")
                .status("IN_SERVICE")
                .purchaseDate(LocalDate.of(2023, 1, 15))
                .build();
    }

    @Test
    @DisplayName("createInstrument - successfully creates instrument")
    void testCreateInstrument_Success() {
        InstrumentCreateRequest request = InstrumentCreateRequest.builder()
                .name("Waters ACQUITY UPLC")
                .model("H-Class")
                .assetCode("EQ-HPLC-001")
                .serialNumber("SN-12345")
                .manufacturer("Waters Corporation")
                .status("IN_SERVICE")
                .build();

        when(instrumentRepository.existsByAssetCode("EQ-HPLC-001")).thenReturn(false);
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(sampleInstrument);

        InstrumentResponse response = instrumentService.createInstrument(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAssetCode()).isEqualTo("EQ-HPLC-001");
        verify(instrumentRepository).save(any(Instrument.class));
    }

    @Test
    @DisplayName("createInstrument - throws exception on duplicate asset code")
    void testCreateInstrument_DuplicateAssetCode() {
        InstrumentCreateRequest request = InstrumentCreateRequest.builder()
                .name("Another HPLC")
                .assetCode("EQ-HPLC-001")
                .build();

        when(instrumentRepository.existsByAssetCode("EQ-HPLC-001")).thenReturn(true);

        assertThatThrownBy(() -> instrumentService.createInstrument(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Asset code already exists");

        verify(instrumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("addCalibrationRecord - successfully adds calibration record")
    void testAddCalibrationRecord_Success() {
        CalibrationRecordRequest request = CalibrationRecordRequest.builder()
                .instrumentId(1L)
                .performedAt(OffsetDateTime.now())
                .nextDue("2027-01-15")
                .performedBy("QA Specialist")
                .notes("All calibration parameters within tolerance")
                .build();

        CalibrationRecord savedRecord = CalibrationRecord.builder()
                .id(10L)
                .instrumentId(1L)
                .performedAt(request.getPerformedAt())
                .nextDue("2027-01-15")
                .performedBy("QA Specialist")
                .notes(request.getNotes())
                .build();

        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(sampleInstrument));
        when(calibrationRepository.save(any(CalibrationRecord.class))).thenReturn(savedRecord);

        CalibrationRecordResponse response = instrumentService.addCalibrationRecord(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getInstrumentAssetCode()).isEqualTo("EQ-HPLC-001");
        verify(calibrationRepository).save(any(CalibrationRecord.class));
    }

    @Test
    @DisplayName("getInstruments - returns paginated responses")
    void testGetInstruments_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Instrument> page = new PageImpl<>(List.of(sampleInstrument), pageable, 1);

        when(instrumentRepository.searchInstruments(null, null, null, pageable)).thenReturn(page);
        when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

        Page<InstrumentResponse> result = instrumentService.getInstruments(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAssetCode()).isEqualTo("EQ-HPLC-001");
    }
}
