package com.vitalys.modules.executive.service;

import com.vitalys.modules.approval.repository.ReportRepository;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.executive.dto.ExecutiveDashboardMetrics;
import com.vitalys.modules.inventory.repository.InventoryLotRepository;
import com.vitalys.modules.retention.repository.LegalHoldRecordRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.stability.repository.StabilityStudyRepository;
import com.vitalys.modules.testing.repository.OosInvestigationRepository;
import com.vitalys.modules.testing.repository.ResultRepository;
import com.vitalys.modules.testing.repository.TestEntityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutiveDashboardServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private TestEntityRepository testEntityRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private OosInvestigationRepository oosInvestigationRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private InventoryLotRepository inventoryLotRepository;

    @Mock
    private StabilityStudyRepository stabilityStudyRepository;

    @Mock
    private LegalHoldRecordRepository legalHoldRecordRepository;

    @InjectMocks
    private ExecutiveDashboardService executiveDashboardService;

    @Test
    @DisplayName("Should aggregate executive KPIs and return monthly throughput")
    void shouldAggregateExecutiveMetrics() {
        when(sampleRepository.count()).thenReturn(25L);
        when(testEntityRepository.count()).thenReturn(60L);
        when(reportRepository.count()).thenReturn(15L);
        when(resultRepository.findByIsOosTrue()).thenReturn(Collections.emptyList());
        when(oosInvestigationRepository.count()).thenReturn(1L);
        when(instrumentRepository.count()).thenReturn(8L);
        when(legalHoldRecordRepository.countByStatus("ACTIVE")).thenReturn(1L);
        when(inventoryLotRepository.findLotsExpiringBefore(any(OffsetDateTime.class))).thenReturn(Collections.emptyList());
        when(stabilityStudyRepository.count()).thenReturn(2L);
        when(testEntityRepository.findAll()).thenReturn(Collections.emptyList());

        ExecutiveDashboardMetrics metrics = executiveDashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalSamplesCount()).isEqualTo(25L);
        assertThat(metrics.getTotalTestsCount()).isEqualTo(60L);
        assertThat(metrics.getTotalCoasSigned()).isEqualTo(15L);
        assertThat(metrics.getActiveLegalHoldsCount()).isEqualTo(1L);
        assertThat(metrics.getFirstTimeRightPercent()).isGreaterThan(95.0);
        assertThat(metrics.getMonthlyThroughput()).hasSize(6);
    }
}
